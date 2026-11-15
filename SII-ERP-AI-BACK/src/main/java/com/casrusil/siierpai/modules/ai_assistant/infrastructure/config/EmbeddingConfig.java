package com.casrusil.siierpai.modules.ai_assistant.infrastructure.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configuration for embeddings and RAG support.
 * Uses all-MiniLM-L6-v2 model for generating embeddings.
 * In-memory store for MVP (can be replaced with persistent store later).
 * 
 * EmbeddingStore is ALWAYS available (needed by DocumentIndexingService).
 * EmbeddingModel is conditional on langchain4j.rag.enabled=true.
 */
@Configuration
public class EmbeddingConfig {

    /**
     * Embedding store - ALWAYS available (in-memory for MVP).
     * Needed by DocumentIndexingService for indexing and search.
     * For production, replace with persistent store (PgVector, Pinecone, etc.)
     */
    @Bean
    @Lazy
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Embedding model for converting text to vectors.
     * all-MiniLM-L6-v2: 384-dimensional embeddings, works well for Spanish.
     * Only created when langchain4j.rag.enabled=true (requires ONNX model download).
     */
    @Bean
    @Lazy
    @ConditionalOnProperty(name = "langchain4j.rag.enabled", havingValue = "true", matchIfMissing = false)
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
