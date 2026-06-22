package com.example.demo.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import com.example.demo.advisor.AdvisorA;
import com.example.demo.advisor.AdvisorB;
import com.example.demo.advisor.AdvisorC;
import com.example.demo.advisor.MaxCharLengthAdvisor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService4 {

  private final ChatClient chatClient;
  private final SafeGuardAdvisor safeGuardAdvisor;

  /**
   * 질문에 민감한 단어가 포함되어 있지 않은 경우에만 프롬프트가 LLM에게 전달 후 응답 받음
   * 민감한 단어가 포함되어 있다면 LLM에게 프롬프트 전달 X, Config에서 설정한 메시지 사용자에게 반환
   * */
  public String advisorSafeGuard(String question) {
    return chatClient.prompt()
            .advisors(safeGuardAdvisor)
            .user(question)
            .call()
            .content();
  }
}