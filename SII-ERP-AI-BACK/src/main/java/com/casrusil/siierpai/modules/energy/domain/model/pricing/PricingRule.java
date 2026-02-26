package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Pricing Rule aggregate root for dynamic pricing.
 * 
 * Defines pricing rules based on time, day, and other factors.
 */
public class PricingRule {
    private final PricingRuleId id;
    private final String name;
    private final PricingType type;
    private final BigDecimal basePrice;
    private final TimeSlot timeSlot;
    private final BigDecimal multiplier;
    private boolean active;
    private final LocalDateTime createdAt;

    private PricingRule(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Pricing rule ID is required");
        this.name = Objects.requireNonNull(builder.name, "Name is required");
        this.type = Objects.requireNonNull(builder.type, "Pricing type is required");
        this.basePrice = Objects.requireNonNull(builder.basePrice, "Base price is required");
        this.timeSlot = builder.timeSlot;
        this.multiplier = builder.multiplier != null ? builder.multiplier : BigDecimal.ONE;
        this.active = builder.active;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");

        if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }

        if (multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }
    }

    /**
     * Creates a new pricing rule.
     */
    public static PricingRule create(
            String name,
            PricingType type,
            BigDecimal basePrice,
            TimeSlot timeSlot,
            BigDecimal multiplier) {

        return builder()
                .id(PricingRuleId.generate())
                .name(name)
                .type(type)
                .basePrice(basePrice)
                .timeSlot(timeSlot)
                .multiplier(multiplier)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Calculates the price for the given timestamp.
     */
    public BigDecimal calculatePrice(LocalDateTime timestamp) {
        if (!active) {
            return basePrice;
        }

        if (timeSlot != null && !timeSlot.contains(timestamp.toLocalTime(), timestamp.getDayOfWeek())) {
            return basePrice;
        }

        return basePrice.multiply(multiplier);
    }

    /**
     * Checks if this rule applies at the given timestamp.
     */
    public boolean appliesAt(LocalDateTime timestamp) {
        if (!active) {
            return false;
        }

        if (timeSlot == null) {
            return true;
        }

        return timeSlot.contains(timestamp.toLocalTime(), timestamp.getDayOfWeek());
    }

    /**
     * Activates the pricing rule.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivates the pricing rule.
     */
    public void deactivate() {
        this.active = false;
    }

    // Getters
    public PricingRuleId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PricingType getType() {
        return type;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PricingRuleId id;
        private String name;
        private PricingType type;
        private BigDecimal basePrice;
        private TimeSlot timeSlot;
        private BigDecimal multiplier;
        private boolean active = true;
        private LocalDateTime createdAt;

        public Builder id(PricingRuleId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(PricingType type) {
            this.type = type;
            return this;
        }

        public Builder basePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder timeSlot(TimeSlot timeSlot) {
            this.timeSlot = timeSlot;
            return this;
        }

        public Builder multiplier(BigDecimal multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PricingRule build() {
            return new PricingRule(this);
        }
    }
}
