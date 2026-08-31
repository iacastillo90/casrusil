package com.casrusil.siierpai.modules.dms.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.dms.application.service.DmsServiceImpl;
import com.casrusil.siierpai.modules.dms.domain.model.CorporateDocument;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dms")
public class DmsController {
    
    private final DmsServiceImpl dmsService;

    public DmsController(DmsServiceImpl dmsService) {
        this.dmsService = dmsService;
    }

    @PostMapping
    public ResponseEntity<CorporateDocument> uploadDocument(@Valid @RequestBody UploadReq req) {
        return ResponseEntity.ok(dmsService.uploadDocument(CompanyContext.requireCompanyId(), req.title(), req.documentType(), req.storageUrl()));
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