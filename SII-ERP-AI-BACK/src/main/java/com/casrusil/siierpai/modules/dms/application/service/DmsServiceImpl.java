package com.casrusil.siierpai.modules.dms.application.service;

import com.casrusil.siierpai.modules.dms.domain.model.CorporateDocument;
import com.casrusil.siierpai.modules.dms.infrastructure.adapter.out.ai.GeminiApiClient;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.entity.CorporateDocumentEntity;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.repository.CorporateDocumentJpaRepository;
import com.casrusil.siierpai.shared.application.port.out.DocumentStoragePort;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DmsServiceImpl {
    private final CorporateDocumentJpaRepository repo;
    private final DocumentStoragePort storagePort;
    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DmsServiceImpl(CorporateDocumentJpaRepository repo,
                          DocumentStoragePort storagePort,
                          GeminiApiClient geminiApiClient) {
        this.repo = repo;
        this.storagePort = storagePort;
        this.geminiApiClient = geminiApiClient;
    }

    public CorporateDocument uploadDocument(CompanyId companyId, String title, String documentType, String storageUrl) {
        CorporateDocumentEntity e = new CorporateDocumentEntity();
        e.setId(UUID.randomUUID());
        e.setCompanyId(companyId.value().toString());
        e.setTitle(title);
        e.setDocumentType(documentType);
        e.setStorageUrl(storageUrl);
        repo.save(e);
        return new CorporateDocument(e.getId(), companyId, title, documentType, storageUrl, null, null);
    }

    public List<CorporateDocument> getDocuments(CompanyId companyId) {
        return repo.findByCompanyId(companyId.value().toString()).stream()
            .map(e -> new CorporateDocument(e.getId(), companyId, e.getTitle(), e.getDocumentType(), e.getStorageUrl(), e.getExpirationDate(), e.getAiSummary()))
            .collect(Collectors.toList());
    }

    /**
     * Analiza un documento corporativo con IA real: descarga el archivo desde el
     * almacenamiento (MinIO), lo envia a Gemini pidiendo un resumen y la fecha de
     * vencimiento en JSON, y persiste el resultado. Reemplaza el resumen hardcodeado.
     */
    public CorporateDocument analyzeDocumentWithAi(CompanyId companyId, UUID documentId) {
        CorporateDocumentEntity e = repo.findById(documentId)
            .filter(d -> d.getCompanyId().equals(companyId.value().toString()))
            .orElseThrow(() -> new RuntimeException("Document not found"));

        byte[] bytes = downloadBytes(e.getStorageUrl());

        String prompt = "Resume este documento corporativo en espanol en no mas de 5 frases e identifica "
                + "su fecha de vencimiento si existe. Responde SOLO en JSON con las claves: "
                + "\"summary\" (string) y \"expirationDate\" (string ISO yyyy-MM-dd o null).";

        String aiResponse = geminiApiClient.summarizeDocument(bytes, "application/pdf", prompt);

        applyAiResult(e, aiResponse);
        repo.save(e);

        return new CorporateDocument(e.getId(), companyId, e.getTitle(), e.getDocumentType(),
                e.getStorageUrl(), e.getExpirationDate(), e.getAiSummary());
    }

    private byte[] downloadBytes(String storageKey) {
        try (var is = storagePort.downloadDocument(storageKey)) {
            return is.readAllBytes();
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo descargar el documento desde el almacenamiento: " + ex.getMessage(), ex);
        }
    }

    /** Parsea la respuesta JSON de la IA y actualiza resumen y vencimiento; si no es JSON valido, guarda el texto crudo. */
    private void applyAiResult(CorporateDocumentEntity e, String aiResponse) {
        try {
            JsonNode node = objectMapper.readTree(aiResponse);
            String summary = node.path("summary").asText(null);
            e.setAiSummary(summary != null && !summary.isBlank() ? summary : aiResponse);
            String exp = node.path("expirationDate").asText(null);
            if (exp != null && !exp.isBlank() && !"null".equalsIgnoreCase(exp)) {
                e.setExpirationDate(LocalDate.parse(exp));
            }
        } catch (Exception parseError) {
            // La IA no devolvio JSON valido: guardamos el texto tal cual como resumen.
            e.setAiSummary(aiResponse);
        }
    }
}