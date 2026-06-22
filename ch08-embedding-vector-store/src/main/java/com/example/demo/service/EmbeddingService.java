package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public void embed(String text) {
        float[] vector = embeddingModel.embed(text);
        log.info("벡터 차원: {}", vector.length);
        log.info("벡터: {}", vector);
    }

    // 사용자가 입력한 텍스트 질문을 임베딩 모델에 전달하여 벡터로 변환
    public void textEmbedding(String question) {
        /**
         * embedForResponse
         * 임베딩 모델의 입력으로 사용할 텍스트 목록을 매개값으로 받음
         * 임베딩 모델의 출력인 벡터와 함께 메타데이터가 포함된 EmbeddingResponse 반환
         * !*! 벡터만 얻고자 하면 embed()를 사용해도 됨 but 사용한 모델의 이름도 얻기 위함
         * */
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(question));

        // 2. 메타데이터 확인
        EmbeddingResponseMetadata metadata = response.getMetadata();
        log.info("모델 이름: {}", metadata.getModel());
        log.info("임베딩 차원: {}", embeddingModel.dimensions());

        // 3. 결과 추출
        Embedding embedding = response.getResults().getFirst();
        float[] vector = embedding.getOutput();

        log.info("벡터 길이: {}", vector.length);
        log.info("벡터 : {}", vector);
    }
}
