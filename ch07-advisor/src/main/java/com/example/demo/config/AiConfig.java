package com.example.demo.config;

import com.example.demo.advisor.MaxCharLengthAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public MaxCharLengthAdvisor maxCharLengthAdvisor() {
        return new MaxCharLengthAdvisor(Ordered.HIGHEST_PRECEDENCE);
    }

    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1);
    }

    @Bean
    public SafeGuardAdvisor safeGuardAdvisor() {
        return new SafeGuardAdvisor(
                List.of("욕설", "계좌번호", "폭력", "폭탄"),
                "해당 질문은 민감한 콘텐츠 요청이므로 응답할 수 없습니다.",
                Ordered.HIGHEST_PRECEDENCE
        );
    }
}
