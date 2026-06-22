package com.example.demo.exam04;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/exam04")
public class Exam04Controller {
  private static final Logger LOGGER = LoggerFactory.getLogger(Exam04Controller.class);
  private ChatClient chatClient;

  public Exam04Controller(ChatModel chatModel, VectorStore vectorStore) {
    this.chatClient = ChatClient.builder(chatModel)
      .defaultAdvisors(
        RetrievalAugmentationAdvisor.builder()
          .queryTransformers(RewriteQueryTransformer.builder()
            .chatClientBuilder(ChatClient.builder(chatModel))
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
  public void method01(HttpSession session) {
    String response = chatClient.prompt()
      .user(
        """
        국회의원은 하는 일 없이 당파 싸움만 하고 있어.
        이래가지고 나라가 발전할 수 있겠어? 
        정말 국회의원 의무는 뭐야?
        """
      )
      .call()
      .content();    
    LOGGER.info(response);
  }     
}
