package com.casrusil.siierpai.modules.dms.infrastructure.web;

import com.casrusil.siierpai.modules.dms.application.service.AutomatedAccountingService;
import com.casrusil.siierpai.modules.dms.application.service.InvoiceAiReaderService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dms/ai-invoicing")
public class AiInvoicingController {

    private final InvoiceAiReaderService aiReaderService;
    private final AutomatedAccountingService accountingService;

    public AiInvoicingController(InvoiceAiReaderService aiReaderService, AutomatedAccountingService accountingService) {
        this.aiReaderService = aiReaderService;
        this.accountingService = accountingService;
    }

    @PostMapping("/process")
    public ResponseEntity<?> processInvoiceUpload(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        CompanyId companyId = CompanyId.fromString(tenantId);
        
        // 1. Leer con Gemini AI
        InvoiceAiReaderService.ExtractedInvoiceData data = aiReaderService.readInvoice(file.getBytes());
        
        // 2. Contabilizar automáticamente (creates draft entry)
        accountingService.createDraftFromInvoice(companyId, data);

        return ResponseEntity.ok(Map.of(
            "status", "PROCESSED",
            "folio", data.folio(),
            "razonSocial", data.razonSocial(),
            "montoTotal", data.montoTotal(),
            "message", "Asiento borrador creado automáticamente por IA"
        ));
    }
}