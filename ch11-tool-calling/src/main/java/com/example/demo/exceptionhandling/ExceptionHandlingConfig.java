package com.example.demo.exceptionhandling;

import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExceptionHandlingConfig {
  //@Bean (사용하려면 주석 처리 해제해야 함, 이렇게 하면 오류 메시지를 LLM으로 전달하는 대신 예외가 애플리케이션으로 던져짐)
  ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
    return new DefaultToolExecutionExceptionProcessor(true);
  }
}
