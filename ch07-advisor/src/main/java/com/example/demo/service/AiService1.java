package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.demo.advisor.AdvisorA;
import com.example.demo.advisor.AdvisorB;
import com.example.demo.advisor.AdvisorC;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService1 {

  private final ChatClient chatClient;

  // ##### 메소드 #####
  /**
   * 전처리: A->B->C 후처리: C->B->A순
   * */

  public String advisorChain1(String question) {
    return chatClient.prompt()
            .advisors(
                    new AdvisorA(),
                    new AdvisorB(),
                    new AdvisorC()
            )
            .user(question)
            .call() //동기
            .content();
  }

  /**
   * 전처리: A->B->C 후처리: C->B->A순
   * */

  public Flux<String> advisorChain2(String question) {
    return chatClient.prompt()
            .advisors(
                    new AdvisorA(),
                    new AdvisorB(),
                    new AdvisorC()
            )
            .user(question)
            .stream() //비동기
            .content();
  }
}
