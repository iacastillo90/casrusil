package com.casrusil.siierpai.modules.ai_assistant.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.ai_assistant.application.service.DocumentIndexingService;
import com.casrusil.siierpai.shared.application.port.out.DocumentStoragePort;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Base de conocimiento de la IA (RAG).
 *
 * <p>Permite subir documentos (leyes tributarias/comerciales, normativa interna,
 * politicas de la empresa) que se almacenan en MinIO y se indexan en la base
 * vectorial (pgvector). Asi el asistente puede responder citando articulos y
 * normas especificas, con aislamiento por empresa.
 *
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li>{@code POST /api/v1/ai/knowledge}: sube e indexa un documento.</li>
 *   <li>{@code GET  /api/v1/ai/knowledge/search}: prueba de recuperacion semantica.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/ai/knowledge")
@PreAuthorize("hasRole('ADMIN')")
public class KnowledgeBaseController {

    private final DocumentIndexingService indexingService;
    private final DocumentStoragePort storagePort;

    public KnowledgeBaseController(DocumentIndexingService indexingService, DocumentStoragePort storagePort) {
        this.indexingService = indexingService;
        this.storagePort = storagePort;
    }

    /**
     * Sube un documento de conocimiento, lo guarda en MinIO y lo indexa en la base
     * vectorial para el RAG.
     *
     * @param file     archivo (PDF, Word, TXT)
     * @param category categoria opcional (ej. LEY_TRIBUTARIA, LEY_COMERCIAL, POLITICA_EMPRESA)
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadKnowledge(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false, defaultValue = "GENERAL") String category) {

        CompanyId companyId = CompanyContext.requireCompanyId();

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo esta vacio"));
        }

        try {
            byte[] bytes = file.getBytes();
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento";
            String storageKey = "knowledge/" + companyId.value() + "/" + UUID.randomUUID() + "-" + originalName;

            // 1. Persistir el archivo original en MinIO.
            storagePort.uploadDocument(storageKey, new ByteArrayInputStream(bytes),
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream");

            // 2. Indexar su contenido en la base vectorial (pgvector) para el RAG.
            int segments = indexingService.indexKnowledgeDocument(companyId, category, originalName, bytes);

            return ResponseEntity.ok(Map.of(
                    "storageKey", storageKey,
                    "category", category,
                    "segmentsIndexed", segments,
                    "message", "Documento almacenado e indexado en la base de conocimiento"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "No se pudo procesar el documento: " + e.getMessage()));
        }
    }

    /**
     * Recupera segmentos de conocimiento relevantes para una consulta (prueba de RAG).
     */
    @GetMapping("/search")
    public ResponseEntity<List<String>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "max", defaultValue = "5") int maxResults) {

        CompanyId companyId = CompanyContext.requireCompanyId();
        List<String> results = indexingService.search(query, companyId, maxResults).stream()
                .map(TextSegment::text)
                .toList();
        return ResponseEntity.ok(results);
    }
}
