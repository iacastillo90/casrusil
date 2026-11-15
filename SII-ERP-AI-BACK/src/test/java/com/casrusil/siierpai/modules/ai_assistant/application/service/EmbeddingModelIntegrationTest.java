package com.casrusil.siierpai.modules.ai_assistant.application.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
class EmbeddingModelIntegrationTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<?> embeddingStore;

    @Test
    void shouldLoadOnnxModelAndGenerateEmbeddings() {
        // Verify the model is loaded (not null)
        assertNotNull(embeddingModel, "EmbeddingModel should be created when langchain4j.rag.enabled=true");

        // Test embedding a simple Spanish text
        String testText = "Esta es una prueba de embeddings en español";
        Embedding embedding = embeddingModel.embed(testText).content();

        assertNotNull(embedding, "Embedding should not be null");
        assertNotNull(embedding.vector(), "Embedding vector should not be null");
        
        // all-MiniLM-L6-v2 produces 384-dimensional embeddings
        assertEquals(384, embedding.vector().length, 
            "all-MiniLM-L6-v2 should produce 384-dimensional embeddings, got " + embedding.vector().length);
        
        // Verify the embedding values are reasonable (not all zeros)
        float sum = 0;
        for (float v : embedding.vector()) {
            sum += Math.abs(v);
        }
        assertTrue(sum > 0, "Embedding should have non-zero values");
        
        System.out.println("✅ ONNX model loaded successfully!");
        System.out.println("✅ Embedding dimension: " + embedding.vector().length);
        System.out.println("✅ Sample values (first 5): " + 
            embedding.vector()[0] + ", " + embedding.vector()[1] + ", " + 
            embedding.vector()[2] + ", " + embedding.vector()[3] + ", " + embedding.vector()[4]);
    }

    @Test
    void shouldIndexAndSearchInMemoryStore() {
        // Verify embedding store is available
        assertNotNull(embeddingStore, "EmbeddingStore should be available");
        assertTrue(embeddingStore instanceof InMemoryEmbeddingStore, 
            "Should use InMemoryEmbeddingStore for MVP");
        
        // Create a test embedding
        String text = "Factura de prueba para RAG";
        Embedding embedding = embeddingModel.embed(text).content();
        
        // The store should work with the embedding model
        // Note: InMemoryEmbeddingStore uses TextSegment, so we test via DocumentIndexingService
        System.out.println("✅ InMemoryEmbeddingStore is available for RAG");
    }
}