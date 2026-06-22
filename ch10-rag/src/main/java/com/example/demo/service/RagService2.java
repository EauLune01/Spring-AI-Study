package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RagService2 {
  // ##### 필드 #####
  private ChatClient chatClient;
  @Autowired
  private ChatModel chatModel;
  @Autowired
  private VectorStore vectorStore;
  @Autowired
  private ChatMemory chatMemory;

  // ##### 생성자 #####
  public RagService2(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
      .defaultAdvisors(
          new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)
      )
      .build();
  }

  // 유사도 검색 기능을 위한 모듈
  // ##### VectorStoreDocumentRetriever 생성하고 반환하는 메소드 #####
  private VectorStoreDocumentRetriever createVectorStoreDocumentRetriever(
    double score, String source) {
    VectorStoreDocumentRetriever vectorStoreDocumentRetriever = 
        VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(score)
            .topK(3)
            .filterExpression(() -> {
                FilterExpressionBuilder builder = new FilterExpressionBuilder();
                if (StringUtils.hasText(source)) {
                  return builder.eq("source", source).build();
                } else {
                  return null;
                }
            })
            .build();
    return vectorStoreDocumentRetriever;
  }

  //-------------------------------------------------------------------------------
  // ##### CompressionQueryTransformer를 생성하고 반환하는 메소드 #####
  private CompressionQueryTransformer createCompressionQueryTransformer() {
    // 새로운 ChatClient를 생성하는 빌더 생성
    // 사용자 질문을 완전한 질문으로 만들기 위해 사용자 질문과 별개의 ChatClient 사용
    ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel)
        .defaultAdvisors(
            new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)    
        );

    // 압축 쿼리 변환기 생성
    CompressionQueryTransformer compressionQueryTransformer = 
        CompressionQueryTransformer.builder()
            .chatClientBuilder(chatClientBuilder)
            .build();

    return compressionQueryTransformer;
  }

  // ##### LLM과 대화하는 메소드 #####
  public String chatWithCompression(String question, double score, String source, String conversationId) {
    // RetrievalAugmentationAdvisor 생성
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = 
        RetrievalAugmentationAdvisor.builder()
            .queryTransformers(createCompressionQueryTransformer())
            .documentRetriever(createVectorStoreDocumentRetriever(score, source))
            .build();

    // 프롬프트를 LLM으로 전송하고 응답을 받는 코드
    String answer = this.chatClient.prompt()
        .user(question)
        .advisors(
          MessageChatMemoryAdvisor.builder(chatMemory).build(), //대화 기록을 프롬프트에 추가하는 Advisor
          retrievalAugmentationAdvisor //완성된 RetrievalAugmentaion Advisor를 추가
        )
        .advisors(advisorSpec -> advisorSpec.param(
            ChatMemory.CONVERSATION_ID, conversationId))
        .call()
        .content();
    return answer;
  }

  //-------------------------------------------------------------------------------
  // ##### RewriteQueryTransformer 생성하고 반환하는 메소드 #####
  private RewriteQueryTransformer createRewriteQueryTransformer() {
    // 새로운 ChatClient 생성하는 빌더 생성
    ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel)
        .defaultAdvisors(
            new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)    
        );

    // 질문 재작성기 생성
    RewriteQueryTransformer rewriteQueryTransformer = 
        RewriteQueryTransformer.builder()
            .chatClientBuilder(chatClientBuilder)
            .build();

    return rewriteQueryTransformer;
  }

  public String chatWithRewriteQuery(String question, double score, String source) {
    // RetrievalAugmentationAdvisor 생성
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = 
        RetrievalAugmentationAdvisor.builder()
            .queryTransformers(createRewriteQueryTransformer())
            .documentRetriever(createVectorStoreDocumentRetriever(score, source))
            .build();

    // 프롬프트를 LLM으로 전송하고 응답을 받는 코드
    String answer = this.chatClient.prompt()
        .user(question)
        .advisors(retrievalAugmentationAdvisor)
        .call()
        .content();
    return answer;
  }  

  //-------------------------------------------------------------------------------
  // ##### TranslationQueryTransformer 생성하고 반환하는 메소드 #####
  private TranslationQueryTransformer createTranslationQueryTransformer() {
    // 새로운 ChatClient를 생성하는 빌더 생성
    ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel)
        .defaultAdvisors(
            new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)
        );

    // 질문 번역기 생성
    TranslationQueryTransformer translationQueryTransformer = 
        TranslationQueryTransformer.builder()
            .chatClientBuilder(chatClientBuilder)
            .targetLanguage("korean") //번역 대상 언어가 한국어
            .build();

    return translationQueryTransformer;
  }

  // ##### LLM과 대화하는 메소드 #####
  public String chatWithTranslation(String question, double score, String source) {
    // RetrievalAugmentationAdvisor 생성
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = 
        RetrievalAugmentationAdvisor.builder()
            .queryTransformers(createTranslationQueryTransformer())
            .documentRetriever(createVectorStoreDocumentRetriever(score, source))
            .build();

    // 프롬프트를 LLM으로 전송하고 응답을 받는 코드
    String answer = this.chatClient.prompt()
        .user(question)
        .advisors(retrievalAugmentationAdvisor)
        .call()
        .content();
    return answer;
  }

  //-------------------------------------------------------------------------------
  // ##### MultiQueryExpander 생성하고 반환하는 메소드 #####
  private MultiQueryExpander createMultiQueryExpander() {
    // 새로운 ChatClient 빌더 생성
    ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel)
        .defaultAdvisors(
            new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)
        );

    // 질문 확장기 생성
    MultiQueryExpander multiQueryExpander = 
        MultiQueryExpander.builder()
            .chatClientBuilder(chatClientBuilder)
            .includeOriginal(true) //원본 질문도 포함
            .numberOfQueries(3) //추가적으로 3개의 확장된 질문을 만듦
            .build();

    return multiQueryExpander;
  }

  // ##### LLM과 대화하는 메소드 #####
  public String chatWithMultiQuery(String question, double score, String source) {
    // RetrievalAugmentationAdvisor 생성
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = 
        RetrievalAugmentationAdvisor.builder()
            .queryExpander(createMultiQueryExpander())
            .documentRetriever(createVectorStoreDocumentRetriever(score, source))
            .build();

    // 프롬프트를 LLM으로 전송하고 응답을 받는 코드
    String answer = this.chatClient.prompt()
        .user(question)
        .advisors(retrievalAugmentationAdvisor)
        .call()
        .content();
    return answer;
  }
}
