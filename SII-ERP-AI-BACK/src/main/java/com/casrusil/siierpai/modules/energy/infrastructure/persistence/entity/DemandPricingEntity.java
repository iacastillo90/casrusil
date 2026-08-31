package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "demand_pricings", schema = "energy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandPricingEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "base_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "peak_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal peakMultiplier;

    @Column(name = "off_peak_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal offPeakMultiplier;

    @Column(name = "demand_threshold_kw", nullable = false)
    private Integer demandThresholdKw;

    @Column(name = "peak_start_time", nullable = false)
    private LocalTime peakStartTime;

    @Column(name = "peak_end_time", nullable = false)
    private LocalTime peakEndTime;

    @Column(name = "applicable_days")
    private String applicableDays; // Stored as comma separated string

    @Column(name = "active", nullable = false)
    private Boolean active;
}
