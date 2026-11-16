package com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoice_audit_log", schema = "invoicing",
        indexes = { @Index(name = "idx_invoice_audit_log_invoice", columnList = "company_id,invoice_id,occurred_at") })
public class InvoiceAuditLogEntity {

    @Id
    private UUID id;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "action", nullable = false, length = 32)
    private String action;

    @Column(name = "username", nullable = false, length = 150)
    private String username;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public InvoiceAuditLogEntity() {
    }

    public InvoiceAuditLogEntity(UUID id, UUID invoiceId, UUID companyId, String action, String username,
            String detail, Instant occurredAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.companyId = companyId;
        this.action = action;
        this.username = username;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getAction() {
        return action;
    }

    public String getUsername() {
        return username;
    }

    public String getDetail() {
        return detail;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
