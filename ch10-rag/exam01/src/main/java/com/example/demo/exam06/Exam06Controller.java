package com.example.demo.exam06;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam06")
public class Exam06Controller {
  private static final Logger LOGGER = LoggerFactory.getLogger(Exam06Controller.class);
  private ChatClient chatClient;

  public Exam06Controller(ChatModel chatModel, VectorStore vectorStore) {
    this.chatClient = ChatClient.builder(chatModel)
      .defaultAdvisors(
        RetrievalAugmentationAdvisor.builder()
          .queryExpander(MultiQueryExpander.builder()
            .chatClientBuilder(ChatClient.builder(chatModel))
            .includeOriginal(true)
            .numberOfQueries(3)
            .build()
          )
        
          .documentRetriever(VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.8)
            .build()
          )
          
          .build()
      )      
      .build();
  } 

  @GetMapping("/method01")
  public void method01() {
    String response = chatClient.prompt()
      .user("비상 급유는 얼마나 넣어줍니까?")
      .call()
      .content();
    LOGGER.info(response);
  } 
}
