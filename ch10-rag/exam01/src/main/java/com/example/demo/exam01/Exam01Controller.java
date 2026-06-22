package com.example.demo.exam01;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam01")
public class Exam01Controller {
  private static final Logger LOGGER = LoggerFactory.getLogger(Exam01Controller.class);

  private ChatClient chatClient1;
  private ChatClient chatClient2;
  private VectorStore vectorStore;

  public Exam01Controller(ChatModel ChatModel, VectorStore vectorStore) {
    this.chatClient1 = ChatClient.builder(ChatModel).build();

    this.chatClient2 = ChatClient.builder(ChatModel)
      .defaultAdvisors(
        new QuestionAnswerAdvisor(
          vectorStore,
          SearchRequest.builder()
            .similarityThreshold(0.8)
            .topK(3)
            .build()
        ), 
        new SimpleLoggerAdvisor(Integer.MAX_VALUE)
      )
      .build();

    this.vectorStore = vectorStore;
  }

  @GetMapping("/method01")
  public void method01() {
    //Extract
    Resource resource = new ClassPathResource("static/documents/대한민국헌법(19880225).pdf");
    DocumentReader reader = new PagePdfDocumentReader(resource);
    List<Document> documents = reader.read();
    for(Document doc : documents) {
      Map<String, Object> metadata = doc.getMetadata();
      metadata.putAll(Map.of(
        "title", "대한민국헌법",
        "author", "법제처",
        "amendment", "9차"
      ));
    }
    LOGGER.info("documents: {}개", documents.size());

    //Transform
    DocumentTransformer transformer = new TokenTextSplitter();
    documents = transformer.apply(documents);
    LOGGER.info("documents: {}개", documents.size());
    
    //Load
    vectorStore.add(documents);
  }

  @GetMapping("/method02")
  public void method02() {
    String response = chatClient1.prompt()
      .advisors(
        new QuestionAnswerAdvisor(
          vectorStore,
          SearchRequest.builder()
            .similarityThreshold(0.8)
            .topK(3)
            .filterExpression("amendment == '9차'")
            .build()
          ),
          new SimpleLoggerAdvisor(Integer.MAX_VALUE)
      )
      .user("대통령 재임 기간은?")
      .call()
      .content();
    LOGGER.info(response);
  }  

  @GetMapping("/method03")
  public void method03() {
    String response = chatClient2.prompt()
      .advisors(a -> a.param(
        QuestionAnswerAdvisor.FILTER_EXPRESSION, "amendment == '9차'"
      ))
      .user("대통령 임기는?")
      .call()
      .content();
    LOGGER.info(response);
  }   
}
