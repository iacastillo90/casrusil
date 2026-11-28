package com.casrusil.siierpai.modules.ai_assistant.application.service;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * H08: la indexación de documentos de conocimiento usa Apache Tika; tras forzar Tika
 * 3.2.2 se verifica que un PDF real se sigue extrayendo e indexando, y que un XML con
 * entidad externa no filtra archivos locales (XXE).
 */
class KnowledgePdfIndexingTest {

    @Test
    void tikaEnLaVersionCorregida() {
        String version = Tika.class.getPackage().getImplementationVersion();
        assertTrue(version == null || version.startsWith("3."), "Se esperaba Tika 3.x, se encontró " + version);
    }

    @Test
    @SuppressWarnings("unchecked")
    void indexaUnPdfDeConocimiento() throws Exception {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        EmbeddingStore<TextSegment> store = mock(EmbeddingStore.class);
        when(embeddingModel.embedAll(anyList())).thenAnswer(inv -> {
            List<TextSegment> segments = inv.getArgument(0);
            return Response.from(segments.stream().map(s -> Embedding.from(new float[] { 1f, 0f })).toList());
        });
        DocumentIndexingService service = new DocumentIndexingService(embeddingModel, store);

        int indexed = service.indexKnowledgeDocument(CompanyId.random(), "LEY_TRIBUTARIA", "iva.pdf",
                pdf("El IVA en Chile es de 19 por ciento sobre el precio neto."));

        assertTrue(indexed >= 1);
        ArgumentCaptor<List<TextSegment>> captor = ArgumentCaptor.forClass(List.class);
        verify(store).addAll(anyList(), captor.capture());
        String text = captor.getValue().get(0).text();
        assertTrue(text.contains("IVA en Chile"), "Texto extraído: " + text);
        assertEquals("knowledge", captor.getValue().get(0).metadata().getString("type"));
    }

    @Test
    void xmlConEntidadExterna_noFiltraArchivosLocales() throws Exception {
        String xxe = "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><r>&x;</r>";
        String extracted = new Tika().parseToString(new java.io.ByteArrayInputStream(
                xxe.getBytes(StandardCharsets.UTF_8)));
        assertFalse(extracted.contains("root:"), "Tika no debe resolver entidades externas");
    }

    private static byte[] pdf(String text) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph(text));
        document.close();
        return out.toByteArray();
    }
}
