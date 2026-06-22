package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import com.example.demo.advisor.MaxCharLengthAdvisor;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService3 {

  private final ChatClient chatClient;
  private final MaxCharLengthAdvisor maxCharLengthAdvisor;
  private final SimpleLoggerAdvisor simpleLoggerAdvisor;

  public String advisorLogging(String question) {
    return chatClient.prompt()
            .advisors(
                    maxCharLengthAdvisor,
                    simpleLoggerAdvisor
            )
            .advisors(spec ->
                    spec.param(MaxCharLengthAdvisor.MAX_CHAR_LENGTH, 100) // 이 정보가 올바르게 프롬프트에 반영되는지 SipleLoggerAdvisor가 출력하는 로그 통해 확인
            )
            .user(question)
            .call()
            .content();
  }
}