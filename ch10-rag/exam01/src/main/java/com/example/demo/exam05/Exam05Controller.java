package com.example.demo.exam05;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam05")
public class Exam05Controller {
  private static final Logger LOGGER = LoggerFactory.getLogger(Exam05Controller.class);
  private ChatClient chatClient;
  private QueryTransformer queryTransformer;

  public Exam05Controller(ChatModel chatModel, VectorStore vectorStore) {
    this.chatClient = ChatClient.builder(chatModel)
      .defaultAdvisors(
        RetrievalAugmentationAdvisor.builder()
          .documentRetriever(VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.8)
            .build()
          )
        .build()
      )
      .build();

    this.queryTransformer = TranslationQueryTransformer.builder()
      .chatClientBuilder(ChatClient.builder(chatModel))
      .targetLanguage("korean")
      .build();
  } 

  @GetMapping("/method01")
  public void method01() {
    Query transformedQuery = queryTransformer.transform(
      new Query("¿Hasta cuándo se puede realizar la reparación?")
    );
    LOGGER.info(transformedQuery.text());

    String response = chatClient.prompt()
      .user(transformedQuery.text())
      .call()
      .content();
    LOGGER.info(response);
    
    response = chatClient.prompt()
      .user("다음 문장을 스페인어로 번역해 주세요\n" + response)
      .call()
      .content();  
    
    LOGGER.info(response);
  }     
}
