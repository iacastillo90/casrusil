package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/**
 * Domain model for demand-based pricing rules.
 * Applies dynamic pricing multipliers based on current demand and time of day.
 */
public class DemandBasedPricing {

    private final DemandPricingId id;
    private final String name;
    private final String description;
    private final BigDecimal baseRate;
    private final BigDecimal peakMultiplier;
    private final BigDecimal offPeakMultiplier;
    private final Integer demandThresholdKw;
    private final LocalTime peakStartTime;
    private final LocalTime peakEndTime;
    private final Set<DayOfWeek> applicableDays;
    private final boolean active;

    private DemandBasedPricing(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.baseRate = builder.baseRate;
        this.peakMultiplier = builder.peakMultiplier;
        this.offPeakMultiplier = builder.offPeakMultiplier;
        this.demandThresholdKw = builder.demandThresholdKw;
        this.peakStartTime = builder.peakStartTime;
        this.peakEndTime = builder.peakEndTime;
        this.applicableDays = builder.applicableDays;
        this.active = builder.active;

        validate();
    }

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Demand pricing name cannot be blank");
        }
        if (baseRate == null || baseRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base rate must be non-negative");
        }
        if (peakMultiplier == null || peakMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Peak multiplier must be positive");
        }
        if (offPeakMultiplier == null || offPeakMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Off-peak multiplier must be positive");
        }
        if (demandThresholdKw == null || demandThresholdKw <= 0) {
            throw new IllegalArgumentException("Demand threshold must be positive");
        }
        if (peakStartTime == null || peakEndTime == null) {
            throw new IllegalArgumentException("Peak times cannot be null");
        }
    }

    /**
     * Calculates the price based on current demand and time.
     */
    public BigDecimal calculatePrice(
            BigDecimal basePrice,
            Integer currentDemandKw,
            LocalDateTime timestamp) {
        if (!isApplicable(timestamp)) {
            return basePrice;
        }

        BigDecimal multiplier = determineMultiplier(currentDemandKw, timestamp.toLocalTime());
        return basePrice.multiply(multiplier);
    }

    /**
     * Determines if this pricing rule is applicable for the given timestamp.
     */
    public boolean isApplicable(LocalDateTime timestamp) {
        if (!active) {
            return false;
        }

        if (applicableDays != null && !applicableDays.isEmpty()) {
            return applicableDays.contains(timestamp.getDayOfWeek());
        }

        return true;
    }

    /**
     * Determines the appropriate multiplier based on demand and time.
     */
    private BigDecimal determineMultiplier(Integer currentDemandKw, LocalTime time) {
        boolean isPeakTime = isWithinPeakHours(time);
        boolean isHighDemand = currentDemandKw >= demandThresholdKw;

        if (isPeakTime && isHighDemand) {
            // Maximum multiplier during peak + high demand
            return peakMultiplier;
        } else if (isPeakTime) {
            // Moderate multiplier during peak time only
            return peakMultiplier.multiply(new BigDecimal("0.8"));
        } else if (isHighDemand) {
            // Moderate multiplier during high demand only
            return BigDecimal.ONE.add(
                    peakMultiplier.subtract(BigDecimal.ONE).multiply(new BigDecimal("0.5")));
        } else {
            // Off-peak multiplier
            return offPeakMultiplier;
        }
    }

    /**
     * Checks if the given time is within peak hours.
     */
    private boolean isWithinPeakHours(LocalTime time) {
        if (peakStartTime.isBefore(peakEndTime)) {
            // Normal case: peak is within same day (e.g., 08:00 - 20:00)
            return !time.isBefore(peakStartTime) && !time.isAfter(peakEndTime);
        } else {
            // Edge case: peak crosses midnight (e.g., 20:00 - 08:00)
            return !time.isBefore(peakStartTime) || !time.isAfter(peakEndTime);
        }
    }

    // Getters
    public DemandPricingId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public BigDecimal getPeakMultiplier() {
        return peakMultiplier;
    }

    public BigDecimal getOffPeakMultiplier() {
        return offPeakMultiplier;
    }

    public Integer getDemandThresholdKw() {
        return demandThresholdKw;
    }

    public LocalTime getPeakStartTime() {
        return peakStartTime;
    }

    public LocalTime getPeakEndTime() {
        return peakEndTime;
    }

    public Set<DayOfWeek> getApplicableDays() {
        return applicableDays;
    }

    public boolean isActive() {
        return active;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DemandPricingId id;
        private String name;
        private String description;
        private BigDecimal baseRate;
        private BigDecimal peakMultiplier;
        private BigDecimal offPeakMultiplier;
        private Integer demandThresholdKw;
        private LocalTime peakStartTime;
        private LocalTime peakEndTime;
        private Set<DayOfWeek> applicableDays;
        private boolean active = true;

        public Builder id(DemandPricingId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder baseRate(BigDecimal baseRate) {
            this.baseRate = baseRate;
            return this;
        }

        public Builder peakMultiplier(BigDecimal peakMultiplier) {
            this.peakMultiplier = peakMultiplier;
            return this;
        }

        public Builder offPeakMultiplier(BigDecimal offPeakMultiplier) {
            this.offPeakMultiplier = offPeakMultiplier;
            return this;
        }

        public Builder demandThresholdKw(Integer demandThresholdKw) {
            this.demandThresholdKw = demandThresholdKw;
            return this;
        }

        public Builder peakStartTime(LocalTime peakStartTime) {
            this.peakStartTime = peakStartTime;
            return this;
        }

        public Builder peakEndTime(LocalTime peakEndTime) {
            this.peakEndTime = peakEndTime;
            return this;
        }

        public Builder applicableDays(Set<DayOfWeek> applicableDays) {
            this.applicableDays = applicableDays;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public DemandBasedPricing build() {
            if (id == null) {
                id = DemandPricingId.generate();
            }
            return new DemandBasedPricing(this);
        }
    }
}
