package com.casrusil.siierpai.modules.ai_assistant.infrastructure.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Configuración de store de embeddings.
 *
 * <p>Usa InMemoryEmbeddingStore para desarrollo. En producción (profile=prod)
 * usa PgVectorEmbeddingStore con pgvector en PostgreSQL.
 * 
 * Conditional on langchain4j.rag.enabled=true.
 */
@Configuration
@ConditionalOnProperty(name = "langchain4j.rag.enabled", havingValue = "true", matchIfMissing = false)
public class PgVectorConfig {

    @Bean
    @Lazy
    @Profile("!prod")
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    @Lazy
    @Profile("prod")
    public EmbeddingStore<TextSegment> pgVectorEmbeddingStore(DataSource dataSource,
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("embeddings")
                .dimension(384)
                .createTable(true)
                .dropTableFirst(false)
                .build();
    }
}
