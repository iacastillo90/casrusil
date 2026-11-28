package com.casrusil.siierpai.modules.dms.infrastructure.web;

import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;

import com.casrusil.siierpai.modules.dms.application.service.AutomatedAccountingService;
import com.casrusil.siierpai.modules.dms.application.service.InvoiceAiReaderService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dms/ai-invoicing")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class AiInvoicingController {

    private final InvoiceAiReaderService aiReaderService;
    private final AutomatedAccountingService accountingService;

    public AiInvoicingController(InvoiceAiReaderService aiReaderService, AutomatedAccountingService accountingService) {
        this.aiReaderService = aiReaderService;
        this.accountingService = accountingService;
    }

    @PostMapping("/process")
    public ResponseEntity<?> processInvoiceUpload(
            @RequestParam("file") MultipartFile file) throws IOException {

        // Empresa del usuario autenticado (JWT). Nunca se acepta desde la petición.
        CompanyId companyId = CompanyContext.requireCompanyId();
        
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