package com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics.ImpactCategory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 🌿 Entidad JPA para persistencia de métricas ambientales.
 * 
 * @see com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics
 * @since 2.0 - Ferrari Engine Phase 2
 */
@Entity
@Table(name = "green_metrics", schema = "sustainability", indexes = {
        @Index(name = "idx_green_metrics_company", columnList = "company_id"),
        @Index(name = "idx_green_metrics_entry", columnList = "accounting_entry_id"),
        @Index(name = "idx_green_metrics_category", columnList = "category")
})
public class GreenMetricsEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "accounting_entry_id", nullable = true)
    private UUID accountingEntryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ImpactCategory category;

    @Column(name = "carbon_footprint", precision = 19, scale = 4, nullable = false)
    private BigDecimal carbonFootprint;

    @Column(name = "green_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal greenScore;

    @Column(name = "evidence", length = 500)
    private String evidence;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // JPA requires no-arg constructor
    protected GreenMetricsEntity() {
    }

    public GreenMetricsEntity(
            UUID id,
            UUID companyId,
            UUID accountingEntryId,
            ImpactCategory category,
            BigDecimal carbonFootprint,
            BigDecimal greenScore,
            String evidence,
            Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.accountingEntryId = accountingEntryId;
        this.category = category;
        this.carbonFootprint = carbonFootprint;
        this.greenScore = greenScore;
        this.evidence = evidence;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getAccountingEntryId() {
        return accountingEntryId;
    }

    public ImpactCategory getCategory() {
        return category;
    }

    public BigDecimal getCarbonFootprint() {
        return carbonFootprint;
    }

    public BigDecimal getGreenScore() {
        return greenScore;
    }

    public String getEvidence() {
        return evidence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
