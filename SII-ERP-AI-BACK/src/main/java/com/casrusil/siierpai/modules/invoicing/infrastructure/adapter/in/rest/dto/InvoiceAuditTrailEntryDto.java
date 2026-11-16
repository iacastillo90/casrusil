package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceAuditLog;

import java.time.Instant;

public record InvoiceAuditTrailEntryDto(
        String id,
        Instant timestamp,
        String action,
        String user,
        String detail) {

    public static InvoiceAuditTrailEntryDto from(InvoiceAuditLog auditLog) {
        return new InvoiceAuditTrailEntryDto(
                auditLog.id().toString(),
                auditLog.occurredAt(),
                auditLog.action(),
                auditLog.username(),
                auditLog.detail());
    }
}
