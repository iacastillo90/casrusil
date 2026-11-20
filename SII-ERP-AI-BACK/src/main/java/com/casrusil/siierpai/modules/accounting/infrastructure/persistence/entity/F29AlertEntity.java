package com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Alerta de F29 desactualizado: un asiento de venta cayó en un período ya
 * cerrado/declarado. No modifica el F29 ni reabre el período.
 */
@Entity
@Table(name = "f29_alerts", schema = "accounting")
public class F29AlertEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "invoice_folio", nullable = false)
    private Long invoiceFolio;

    @Column(name = "vat_difference")
    private BigDecimal vatDifference;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    public F29AlertEntity() {
    }

    public F29AlertEntity(UUID id, UUID companyId, Integer periodYear, Integer periodMonth, Long invoiceFolio,
            BigDecimal vatDifference, String reason, Instant createdAt, boolean resolved) {
        this.id = id;
        this.companyId = companyId;
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
        this.invoiceFolio = invoiceFolio;
        this.vatDifference = vatDifference;
        this.reason = reason;
        this.createdAt = createdAt;
        this.resolved = resolved;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public Integer getPeriodYear() {
        return periodYear;
    }

    public Integer getPeriodMonth() {
        return periodMonth;
    }

    public Long getInvoiceFolio() {
        return invoiceFolio;
    }

    public BigDecimal getVatDifference() {
        return vatDifference;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isResolved() {
        return resolved;
    }
}
