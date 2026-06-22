package com.example.demo.controller;

import java.io.IOException;
import java.util.List;

import com.example.demo.service.EmbeddingService;
import com.example.demo.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.FaceService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

  private final EmbeddingService embeddingService;
  private final VectorStoreService vectorStoreService;
  private final FaceService faceService;

  @PostMapping(
          value = "/text-embedding",
          consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
          produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String textEmbedding(@RequestParam("question") String question) {
    embeddingService.textEmbedding(question);
    return "서버 터미널(콘솔) 출력을 확인하세요.";
  }

  @PostMapping(
          value = "/add-document",
          consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
          produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String addDocument() {
    vectorStoreService.addDocument();
    return "벡터 저장소에 Document들이 저장되었습니다.";
  }

  @PostMapping(
          value = "/search-document-1",
          consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
          produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String searchDocument1(@RequestParam("question") String question) {
    List<Document> documents = vectorStoreService.search(question, 3, 0.4);
    return formatDocuments(documents);
  }

  @PostMapping(
          value = "/search-document-2",
          consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
          produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String searchDocument2(@RequestParam("question") String question) {
    List<Document> documents = vectorStoreService.searchWithFilter(question);
    return formatDocuments(documents);
  }

  @PostMapping(
          value = "/delete-document",
          consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
          produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String deleteDocument() {
    vectorStoreService.deleteByFilter("source == '헌법' && year < 1987");
    return "Document들이 삭제되었습니다.";
  }

  @PostMapping(
          value = "/add-face",
          consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
          produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String addFace(
          @RequestParam("personName") String personName,
          @RequestParam("attach") MultipartFile[] attach
  ) throws IOException {
    for (MultipartFile mf : attach) {
      faceService.addFace(personName, mf);
    }
    return "얼굴이 저장되었습니다.";
  }

  @PostMapping(
          value = "/find-face",
          consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
          produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String findFace(@RequestParam("attach") MultipartFile attach) throws IOException {
    return faceService.findFace(attach);
  }

  private String formatDocuments(List<Document> documents) {
    StringBuilder text = new StringBuilder();

    for (Document document : documents) {
      text.append("<div class='mb-2'>")
              .append("<span class='me-2'>유사도 점수: ")
              .append(document.getScore())
              .append(",</span>")
              .append("<span>")
              .append(document.getText())
              .append("(")
              .append(document.getMetadata().get("year"))
              .append(")</span>")
              .append("</div>");
    }

    return text.toString();
  }
}