package com.casrusil.siierpai.modules.dms.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.dms.application.service.DmsServiceImpl;
import com.casrusil.siierpai.modules.dms.domain.model.CorporateDocument;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dms")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class DmsController {
    
    private final DmsServiceImpl dmsService;

    public DmsController(DmsServiceImpl dmsService) {
        this.dmsService = dmsService;
    }

    /**
     * Registro por metadatos (compatibilidad): el archivo ya está en una URL.
     */
    @PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CorporateDocument> uploadDocument(@Valid @RequestBody UploadReq req) {
        return ResponseEntity.ok(dmsService.uploadDocument(CompanyContext.requireCompanyId(), req.title(), req.documentType(), req.storageUrl()));
    }

    /**
     * Registro con archivo (multipart). El archivo se guarda en el almacenamiento
     * del sistema (MinIO/S3) y se valida tamaño y tipo MIME.
     *
     * @param file         archivo (máx. 10 MB por defecto; PDF, PNG, JPEG, TXT, DOCX, XLSX)
     * @param title        título visible (opcional, por defecto el nombre del archivo)
     * @param documentType tipo lógico del documento (CONTRATO, FACTURA, …)
     */
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CorporateDocument> uploadDocumentFile(
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("documentType") String documentType) {
        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }
        return ResponseEntity.ok(dmsService.uploadDocumentWithFile(
                CompanyContext.requireCompanyId(), title, documentType.trim(), file));
    }

    @GetMapping
    public ResponseEntity<List<CorporateDocument>> getDocuments() {
        return ResponseEntity.ok(dmsService.getDocuments(CompanyContext.requireCompanyId()));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<CorporateDocument> analyzeDocument(@PathVariable UUID id) {
        return ResponseEntity.ok(dmsService.analyzeDocumentWithAi(CompanyContext.requireCompanyId(), id));
    }

    public record UploadReq(String title, String documentType, String storageUrl) {}
}