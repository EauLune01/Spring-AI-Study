package com.example.demo.exam03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/exam03")
public class Exam03Controller {
  private static final Logger LOGGER = LoggerFactory.getLogger(Exam03Controller.class);
  private ChatClient chatClient;
  private ChatMemory chatMemory;

  public Exam03Controller(ChatModel chatModel, VectorStore vectorStore) {
    this.chatMemory = new InMemoryChatMemory();
    this.chatClient = ChatClient.builder(chatModel)
      .defaultAdvisors(
        RetrievalAugmentationAdvisor.builder()
          .queryTransformers(CompressionQueryTransformer.builder()
            .chatClientBuilder(ChatClient.builder(chatModel))
            .build()
          )
          .documentRetriever(VectorStoreDocumentRetriever.builder()
              .vectorStore(vectorStore)
              .similarityThreshold(0.8)
              .build())
          .build(),

        new SimpleLoggerAdvisor(Integer.MAX_VALUE - 1)
      )
      .build();    
  }  

  @GetMapping("/method01")
  public void method01(HttpSession session) {
    String response = chatClient.prompt()
      .advisors(MessageChatMemoryAdvisor.builder(this.chatMemory)
        .conversationId(session.getId())
        .build()
      )
      .user("대통령의 임기는 몇 년입니까?")
      .call()
      .content();
    LOGGER.info(response);
  }  

  @GetMapping("/method02")
  public void method02(HttpSession session) {
    String response = chatClient.prompt()
      .advisors(MessageChatMemoryAdvisor.builder(this.chatMemory)
        .conversationId(session.getId())
        .build()
      )    
      .user("국회의원은?")
      .call()
      .content();
    LOGGER.info(response);
  }     
}
