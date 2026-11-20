package com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.accounting.application.service.F29AlertService;
import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.F29AlertEntity;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Alertas de F29 desactualizado (propuesta nueva): un asiento cayó en un
 * período ya cerrado/declarado.
 */
@RestController
@RequestMapping("/api/v1/compliance/f29")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class F29AlertController {

    private final F29AlertService alertService;

    public F29AlertController(F29AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<F29AlertResponse>> alerts() {
        UUID companyId = CompanyContext.requireCompanyId().value();
        return ResponseEntity.ok(alertService.listAlerts(companyId).stream()
                .map(F29AlertResponse::from)
                .toList());
    }

    public record F29AlertResponse(
            UUID id,
            Integer periodYear,
            Integer periodMonth,
            Long invoiceFolio,
            BigDecimal vatDifference,
            String reason,
            Instant createdAt,
            boolean resolved) {

        static F29AlertResponse from(F29AlertEntity entity) {
            return new F29AlertResponse(entity.getId(), entity.getPeriodYear(), entity.getPeriodMonth(),
                    entity.getInvoiceFolio(), entity.getVatDifference(), entity.getReason(),
                    entity.getCreatedAt(), entity.isResolved());
        }
    }
}
