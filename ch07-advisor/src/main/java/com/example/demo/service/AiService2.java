package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import com.example.demo.advisor.MaxCharLengthAdvisor;

import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
public class AiService2 {

  private final ChatClient chatClient;
  private final MaxCharLengthAdvisor maxCharLengthAdvisor;

  public String advisorContext(String question) {
    return chatClient.prompt()
            .advisors(maxCharLengthAdvisor)
            .advisors(spec ->
                    spec.param(MaxCharLengthAdvisor.MAX_CHAR_LENGTH, 100) //공유 데이터에 키 상수로 값 100을 저장
            )
            .user(question)
            .call()
            .content();
  }
}
