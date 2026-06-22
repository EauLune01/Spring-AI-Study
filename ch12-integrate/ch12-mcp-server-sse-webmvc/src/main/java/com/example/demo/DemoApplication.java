package com.example.demo;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.demo.tool.BoomBarrierTools;
import com.example.demo.tool.CarCheckTools;
import com.example.demo.tool.DateTimeTools;
import com.example.demo.tool.FileSystemTools;
import com.example.demo.tool.InternetSearchTools;

@SpringBootApplication
public class DemoApplication {
  @Autowired
  private DateTimeTools dateTimeTools;

  @Autowired
  private FileSystemTools fileSystemTools1;

  @Autowired
  private InternetSearchTools internetSearchTools;

  @Autowired
  private BoomBarrierTools boomBarrierTools;

  @Autowired
  private CarCheckTools carCheckTools;

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @Bean
  public ToolCallbackProvider getToolCallbackProvider() {
    return MethodToolCallbackProvider.builder()
        .toolObjects(
            dateTimeTools, fileSystemTools1, 
            internetSearchTools, boomBarrierTools, carCheckTools)
        .build();
  }
}
