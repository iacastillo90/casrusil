package com.casrusil.siierpai.modules.invoicing.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.Instant;
import java.util.UUID;

public record InvoiceAuditLog(
        UUID id,
        UUID invoiceId,
        CompanyId companyId,
        String action,
        String username,
        String detail,
        Instant occurredAt) {

    public static final String ACTION_CREATED = "CREATED";
    public static final String ACTION_UPDATED = "UPDATED";
    public static final String ACTION_SENT = "SENT";
    public static final String ACTION_STATUS_UPDATED = "STATUS_UPDATED";
    public static final String SYSTEM_USER = "Sistema";

    public static InvoiceAuditLog record(CompanyId companyId, UUID invoiceId, String action, String username,
            String detail) {
        return new InvoiceAuditLog(
                UUID.randomUUID(),
                invoiceId,
                companyId,
                action,
                username != null && !username.isBlank() ? username : SYSTEM_USER,
                detail,
                Instant.now());
    }
}
