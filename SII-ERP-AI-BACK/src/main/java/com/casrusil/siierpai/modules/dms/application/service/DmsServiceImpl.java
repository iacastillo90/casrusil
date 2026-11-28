package com.casrusil.siierpai.modules.dms.application.service;

import com.casrusil.siierpai.modules.dms.domain.model.CorporateDocument;
import com.casrusil.siierpai.modules.dms.infrastructure.adapter.out.ai.GeminiApiClient;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.entity.CorporateDocumentEntity;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.repository.CorporateDocumentJpaRepository;
import com.casrusil.siierpai.shared.application.port.out.DocumentStoragePort;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DmsServiceImpl {
    /** Límite por archivo del módulo (el contenedor admite 20 MB). */
    private static final long DEFAULT_MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private static final String DEFAULT_ALLOWED_CONTENT_TYPES =
            "application/pdf,image/png,image/jpeg,text/plain,"
                    + "application/vnd.openxmlformats-officedocument.wordprocessingml.document,"
                    + "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final CorporateDocumentJpaRepository repo;
    private final DocumentStoragePort storagePort;
    private final GeminiApiClient geminiApiClient;
    private final long maxSizeBytes;
    private final Set<String> allowedContentTypes;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DmsServiceImpl(CorporateDocumentJpaRepository repo,
                          DocumentStoragePort storagePort,
                          GeminiApiClient geminiApiClient,
                          @Value("${dms.upload.max-size-bytes:" + DEFAULT_MAX_SIZE_BYTES + "}") long maxSizeBytes,
                          @Value("${dms.upload.allowed-content-types:" + DEFAULT_ALLOWED_CONTENT_TYPES + "}") String allowedContentTypes) {
        this.repo = repo;
        this.storagePort = storagePort;
        this.geminiApiClient = geminiApiClient;
        this.maxSizeBytes = maxSizeBytes;
        this.allowedContentTypes = Arrays.stream(allowedContentTypes.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
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

    /**
     * Registra un documento subiendo el archivo al almacenamiento y guardando
     * sus metadatos. Valida tamaño y tipo MIME antes de tocar el storage.
     *
     * @param companyId     empresa
     * @param title         título visible (si viene vacío se usa el nombre del archivo)
     * @param documentType  tipo lógico (CONTRATO, FACTURA, …)
     * @param file          archivo multipart
     * @return documento registrado
     */
    public CorporateDocument uploadDocumentWithFile(CompanyId companyId, String title, String documentType,
                                                    MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo es obligatorio y no puede estar vacío");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "El archivo supera el máximo permitido de " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido: " + contentType
                            + ". Permitidos: " + String.join(", ", allowedContentTypes));
        }

        String originalName = baseName(file.getOriginalFilename());
        String safeName = sanitizeFileName(originalName);
        String storageKey = companyId.value() + "/" + UUID.randomUUID() + "-" + safeName;

        try (InputStream stream = file.getInputStream()) {
            storagePort.uploadDocument(storageKey, stream, contentType);
        } catch (IOException ioError) {
            throw new IllegalArgumentException("No se pudo leer el archivo: " + ioError.getMessage(), ioError);
        } catch (Exception storageError) {
            throw new RuntimeException(
                    "No se pudo guardar el archivo en el almacenamiento: " + storageError.getMessage(),
                    storageError);
        }

        String resolvedTitle = (title == null || title.isBlank())
                ? stripExtension(originalName)
                : title.trim();

        return uploadDocument(companyId, resolvedTitle, documentType, storageKey);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        int separator = contentType.indexOf(';');
        String base = separator >= 0 ? contentType.substring(0, separator) : contentType;
        return base.trim().toLowerCase(Locale.ROOT);
    }

    /** Nombre sin ruta, tal cual lo envió el cliente (conserva espacios). */
    private String baseName(String originalFilename) {
        String name = originalFilename == null ? "" : originalFilename;
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return lastSlash >= 0 ? name.substring(lastSlash + 1) : name;
    }

    /** Evita rutas y caracteres raros en la clave del storage. */
    private String sanitizeFileName(String fileName) {
        String name = fileName == null ? "" : fileName;
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            name = "documento";
        }
        return name;
    }

    private String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
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
            .orElseThrow(() -> new com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException(
                    "Documento no encontrado"));

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