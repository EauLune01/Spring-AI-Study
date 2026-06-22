package com.example.demo.exam02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam02")
public class Exam02Controller2 {
  private static final Logger LOGGER = LoggerFactory.getLogger(Exam02Controller2.class);
  private ChatClient chatClient;

  public Exam02Controller2(ChatModel chatModel, VectorStore vectorStore) {
    RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
      .documentRetriever(VectorStoreDocumentRetriever.builder()
        .vectorStore(vectorStore)
        .similarityThreshold(0.8)
        .topK(3)
        .build())
      .build();

    this.chatClient = ChatClient.builder(chatModel)
      .defaultAdvisors(ragAdvisor)
      .build();
  }

  @GetMapping("/method05")
  public void method05() {
    String response = chatClient.prompt()
        .advisors(a -> a.param(
            VectorStoreDocumentRetriever.FILTER_EXPRESSION,
            "title=='자동차보험' && author=='삼성화재'"))
        .user("타이어 교체가 포함되어 있습니까?")
        .call()
        .content();
    LOGGER.info(response);
  }
}
