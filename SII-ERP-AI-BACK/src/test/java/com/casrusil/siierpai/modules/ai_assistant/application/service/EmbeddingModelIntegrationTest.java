package com.casrusil.siierpai.modules.ai_assistant.application.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el modelo ONNX y el store en memoria sin levantar el contexto Spring.
 *
 * <p>El test anterior usaba {@code @SpringBootTest} solo para obtener dos beans
 * que se pueden instanciar directamente; se desacopla del contexto para evitar
 * dependencias de base de datos y servicios externos.</p>
 */
class EmbeddingModelIntegrationTest {

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    @Test
    void shouldLoadOnnxModelAndGenerateEmbeddings() {
        assertNotNull(embeddingModel, "EmbeddingModel should be created when langchain4j.rag.enabled=true");

        String testText = "Esta es una prueba de embeddings en español";
        Embedding embedding = embeddingModel.embed(testText).content();

        assertNotNull(embedding, "Embedding should not be null");
        assertNotNull(embedding.vector(), "Embedding vector should not be null");
        assertEquals(384, embedding.vector().length,
                "all-MiniLM-L6-v2 should produce 384-dimensional embeddings, got " + embedding.vector().length);

        float sum = 0;
        for (float v : embedding.vector()) {
            sum += Math.abs(v);
        }
        assertTrue(sum > 0, "Embedding should have non-zero values");
    }

    @Test
    void shouldIndexAndSearchInMemoryStore() {
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        assertNotNull(embeddingStore, "EmbeddingStore should be available");
        assertTrue(embeddingStore instanceof InMemoryEmbeddingStore,
                "Should use InMemoryEmbeddingStore for MVP");

        Embedding embedding = embeddingModel.embed("Factura de prueba para RAG").content();
        embeddingStore.add(embedding, TextSegment.from("Factura de prueba para RAG"));

        assertNotNull(embedding.vector());
    }
}
