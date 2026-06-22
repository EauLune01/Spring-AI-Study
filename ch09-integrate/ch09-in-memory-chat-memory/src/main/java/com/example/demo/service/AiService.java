package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

  private final ChatClient aiChatClient;

  //사용자의 질문을 LLM으로 전송하고 그에 대한 응답을 받음
  public String chat(String userText, String conversationId) {
    return aiChatClient.prompt()
            .user(userText)
            /**
             * MessageChatMemoryAdvisor, PromptChatMemoryAdvisor는 공유 데이터에서 conversationId를 얻어
             * 기억 저장소에서 검색 조건으로 사용하기도 하고, 대화 기억 저장 시에도 사용
             * */
            .advisors(advisorSpec -> advisorSpec.param(
                    ChatMemory.CONVERSATION_ID, conversationId
            ))
            .call()
            .content();
  }
}
