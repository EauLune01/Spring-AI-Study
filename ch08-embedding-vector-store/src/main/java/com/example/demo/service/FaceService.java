package com.example.demo.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaceService {

  private final JdbcTemplate jdbcTemplate; //SQL문으로 데이터 저장하고 검색
  private final WebClient webClient; //REST API 클라이언트

  // ##### 메소드 #####
  //사진 속 얼굴에 대한 임베딩 벡터 반환
  public float[] getFaceVector(MultipartFile mf) throws IOException {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", mf.getBytes())
      .filename(mf.getOriginalFilename())
      .contentType(MediaType.valueOf(mf.getContentType()));
    MultiValueMap<String, HttpEntity<?>> multipartForm = builder.build();

    //face-embed-api로 사진 이미지를 보내고 응답으로 얼굴 벡터를 JSON으로 얻기
    FaceEmbedApiResponse response = webClient.post()
        .uri("http://localhost:50001/get-face-vector")
        .body(BodyInserters.fromMultipartData(multipartForm))
        .retrieve()
        .bodyToMono(FaceEmbedApiResponse.class)
        .block();
    
    float[] vector = response.vector();
    return vector;
  }
  
  public record FaceEmbedApiResponse(float[] vector) {
  }

  //얼굴 임베딩 벡터를 벡터 저장소에 저장
  public void addFace(String personName, MultipartFile mf) throws IOException {  
      // 얼굴 임베딩
      float[] vector = getFaceVector(mf);
      
      // 벡터 저장소에 저장
      String strVector = Arrays.toString(vector).replace(" ", "");
      String sql = """
          INSERT INTO face_vector_store (content, embedding) 
          VALUES (?, ?::vector)
          """;
      jdbcTemplate.update(sql, personName, strVector);
  }

  //사진 속 얼굴이 누구인지 이름을 반환
  public String findFace(MultipartFile mf) throws IOException {
    // 얼굴 임베딩
    float[] vector = getFaceVector(mf);
    String strVector = Arrays.toString(vector).replace(" ", "");
    // 유사한 얼굴 찾기(<=>): L2 정규화가 되어있을 경우 L2 거리, 0~2, 작을수록 유사
    String sql = """
        SELECT content, (embedding <=> ?::vector) AS similarity
        FROM face_vector_store 
        ORDER BY embedding <=> ?::vector  //벡터 거리 기준으로 작은 순서대로 정렬
        LIMIT 3
        """;
    // 검색 결과를 출력해보기
    List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, strVector, strVector);
    for(Map<String, Object> map : list) {
      String personName = (String) map.get("content");
      Double similarity = (Double) map.get("similarity");
      log.info("{} (L2 거리: {})", personName, similarity);
    }    
    
    // 검색 결과에서 거리가 가장 짧은 벡터의 유사도가 임계값 0.3 이상일 경우
    double similarity = (Double) list.get(0).get("similarity");
    if(similarity > 0.3) {
      return "등록된 사람이 아닙니다.";
    }
    
    // 거리가 가장 짧은 사람의 이름 반환
    return (String) list.get(0).get("content");
  }
}
