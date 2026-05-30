package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Pricing Rules.
 * 
 * <p>
 * Maps to the energy.pricing_rules table for persistent storage
 * of dynamic pricing configurations.
 * 
 * @since 1.0
 */
@Entity
@Table(name = "pricing_rules", schema = "energy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRuleEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "base_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "peak_multiplier", precision = 5, scale = 2)
    private BigDecimal peakMultiplier;

    @Column(name = "off_peak_multiplier", precision = 5, scale = 2)
    private BigDecimal offPeakMultiplier;

    @Column(name = "time_windows", columnDefinition = "jsonb")
    private String timeWindows;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
