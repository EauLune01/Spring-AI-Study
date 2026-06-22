package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * MessageChatMemoryAdvisor 추가: 대화 기억에 해당하는 UserMessage와 AssistantMessage들이
     * 프롬프트에 온전한 메시지 형태로 추가
     *
     * PromptChatMemoryAdvisor 추가: SystemMessage 내용으로
     * 대화 기억에 해당하는 사용자 텍스트와 응답 텍스트가 포함되어 있음
     * */
    @Bean
    public ChatClient aiChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        //PromptChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1) //가장 낮은 실행 순위
                )
                .build();
    }
}
