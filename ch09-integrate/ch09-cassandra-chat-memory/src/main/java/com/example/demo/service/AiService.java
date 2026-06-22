package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.cassandra.CassandraChatMemoryRepository;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

  private final ChatClient chatClient;

  public String chat(String userText, String conversationId) {

    log.info("chat request: conversationId={}, message={}", conversationId, userText);

    return chatClient.prompt()
            .user(userText)
            .advisors(advisorSpec -> advisorSpec.param(
                    ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
  }
}
