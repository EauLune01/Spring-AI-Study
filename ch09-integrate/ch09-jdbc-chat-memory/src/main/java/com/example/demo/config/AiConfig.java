package com.example.demo.config;

@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20) //생략해도 기본 20개
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 ChatMemory chatMemory) {

        return builder
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)
                )
                .build();
    }
}
