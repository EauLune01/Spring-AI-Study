package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class @Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

  private final ChatClient chatClient;

  public String chat(String userText, String conversationId) {

    return chatClient.prompt()
            .user(userText)
            .advisors(advisorSpec -> advisorSpec.param(
                    ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
  }
}