package com.example.demo.exam02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam02")
public class Exam02Controller1 {
  private static final Logger LOGGER = LoggerFactory.getLogger(Exam02Controller1.class);

  private ChatClient chatClient;
  private VectorStore vectorStore;

  public Exam02Controller1(ChatModel chatModel, VectorStore vectorStore) {
    this.chatClient = ChatClient.builder(chatModel).build();
    this.vectorStore = vectorStore;
  }

  @GetMapping("/method01")
  public void method01() {
    Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
      .documentRetriever(VectorStoreDocumentRetriever.builder()
          .vectorStore(vectorStore)
          .similarityThreshold(0.8)
          .build())
      .build();

    String response = chatClient.prompt()
      .advisors(retrievalAugmentationAdvisor, new SimpleLoggerAdvisor(Integer.MAX_VALUE-1))
      .user("수리 인정 기간은 어떻게 됩니까?")
      .call()
      .content();

    LOGGER.info(response);
  }

  @GetMapping("/method02")
  public void method02() {
    Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
      .documentRetriever(VectorStoreDocumentRetriever.builder()
        .vectorStore(vectorStore)
        .similarityThreshold(0.8)
        .build()
      )
      // .queryAugmenter(ContextualQueryAugmenter.builder()
      //   .allowEmptyContext(true)
      //   .build())
      .build();

    String response = chatClient.prompt()
        .advisors(retrievalAugmentationAdvisor, new SimpleLoggerAdvisor(Integer.MAX_VALUE))
        .user("크리스마스 선물로 주는 것은?")
        .call()
        .content();

    LOGGER.info(response);
  } 

  @GetMapping("/method03")
  public void method03() {
    String response = chatClient.prompt()
      .advisors(
        RetrievalAugmentationAdvisor.builder()
          .documentRetriever(VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.8)
            .filterExpression(new FilterExpressionBuilder()
              .eq("title", "자동차보험")
              .build()
            )
            .filterExpression(new FilterExpressionBuilder()
              .eq("author", "삼성화재")
              .build()
            )
            .topK(3)
            .build()
          )
          .build()
      )
      .user("타이어 교체가 포함되어 있습니까?")
      .call()
      .content();
    LOGGER.info(response);
  } 

  @GetMapping("/method04")
  public void method04() {
    String response = chatClient.prompt()
      .advisors(
        RetrievalAugmentationAdvisor.builder()
          .documentRetriever(VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.8)
            .filterExpression(() -> {
              FilterExpressionBuilder feb = new FilterExpressionBuilder();
              return feb
                .and(
                  feb.eq("title", "자동차보험"),
                  feb.eq("author", "삼성화재")
                )
                .build();
            })
            .topK(3)
            .build()
          )
          .build()
      )
      .user("타이어 교체가 포함되어 있습니까?")
      .call()
      .content();
    LOGGER.info(response);
  }  
}
