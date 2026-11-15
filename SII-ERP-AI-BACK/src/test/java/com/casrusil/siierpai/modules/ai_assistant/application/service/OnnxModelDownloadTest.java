package com.casrusil.siierpai.modules.ai_assistant.application.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to verify the ONNX all-MiniLM-L6-v2 model downloads and works correctly.
 * This test runs without Spring context - directly instantiates the model.
 * First run will download the model from HuggingFace (~30-60s).
 */
class OnnxModelDownloadTest {

    @Test
    void shouldDownloadOnnxModelAndGenerate384DimEmbeddings() {
        System.out.println("🔄 Loading AllMiniLmL6V2EmbeddingModel (first run downloads ONNX from HuggingFace)...");
        long startTime = System.currentTimeMillis();
        
        AllMiniLmL6V2EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();
        
        long loadTime = System.currentTimeMillis() - startTime;
        System.out.println("✅ Model loaded in " + loadTime + "ms");
        
        // Test embedding generation
        String testText = "Esta es una prueba de embeddings en español para RAG";
        System.out.println("🔄 Generating embedding for: " + testText);
        
        long embedStart = System.currentTimeMillis();
        Embedding embedding = model.embed(testText).content();
        long embedTime = System.currentTimeMillis() - embedStart;
        
        System.out.println("✅ Embedding generated in " + embedTime + "ms");
        
        // Verify embedding
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
        
        System.out.println("✅ ONNX model working correctly!");
        System.out.println("✅ Embedding dimension: " + embedding.vector().length);
        System.out.println("✅ Sample values (first 5): " + 
            embedding.vector()[0] + ", " + embedding.vector()[1] + ", " + 
            embedding.vector()[2] + ", " + embedding.vector()[3] + ", " + embedding.vector()[4]);
        System.out.println("✅ Vector magnitude (sum of abs): " + sum);
    }
    
    @Test
    void shouldGenerateConsistentEmbeddingsForSameText() {
        AllMiniLmL6V2EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();
        
        String text = "Factura electrónica número 123";
        Embedding emb1 = model.embed(text).content();
        Embedding emb2 = model.embed(text).content();
        
        // Same text should produce identical embeddings
        for (int i = 0; i < emb1.vector().length; i++) {
            assertEquals(emb1.vector()[i], emb2.vector()[i], 0.0001f, 
                "Embeddings should be deterministic for same input at index " + i);
        }
        
        System.out.println("✅ Embeddings are deterministic for same input");
    }
}