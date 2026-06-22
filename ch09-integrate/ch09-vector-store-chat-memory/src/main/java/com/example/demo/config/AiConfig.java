package com.example.demo.config;

@Configuration
public class AiConfig {

    // PostgreSQL(pgvector)에 임베딩 벡터를 저장하고 검색하는 VectorStore를 생성
    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate,
                                   EmbeddingModel embeddingModel) {

        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .initializeSchema(false)
                .schemaName("public")
                .vectorTableName("chat_memory_vector_store")
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 VectorStore vectorStore) {

        return builder
                .defaultAdvisors(
                        VectorStoreChatMemoryAdvisor.builder(vectorStore).build(), //생성 시 VectorStore()를 builder() 매개값으로 전달
                        new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1) //SimpleLoggerAdvisor도 추가
                )
                .build();
    }
}
