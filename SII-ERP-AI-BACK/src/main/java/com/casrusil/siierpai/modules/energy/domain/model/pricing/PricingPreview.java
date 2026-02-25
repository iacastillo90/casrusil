package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Value object representing a pricing preview before charging starts.
 * Shows the breakdown of how the final price was calculated.
 */
public record PricingPreview(
        BigDecimal basePrice,
        BigDecimal demandAdjustedPrice,
        BigDecimal seasonalPrice,
        BigDecimal finalPrice,
        List<PriceAdjustment> breakdown,
        LocalDateTime calculatedAt) {

    public PricingPreview {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price must be non-negative");
        }
        if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final price must be non-negative");
        }
        if (breakdown == null) {
            breakdown = new ArrayList<>();
        }
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }

    /**
     * Calculates the total discount amount.
     */
    public BigDecimal getTotalDiscount() {
        return basePrice.subtract(finalPrice);
    }

    /**
     * Calculates the discount percentage.
     */
    public BigDecimal getDiscountPercentage() {
        if (basePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getTotalDiscount()
                .multiply(new BigDecimal("100"))
                .divide(basePrice, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Represents a single price adjustment in the breakdown.
     */
    public record PriceAdjustment(
            String type,
            String description,
            BigDecimal originalAmount,
            BigDecimal adjustedAmount,
            BigDecimal difference) {
        public PriceAdjustment {
            if (type == null || type.isBlank()) {
                throw new IllegalArgumentException("Adjustment type cannot be blank");
            }
        }

        public static PriceAdjustment demandBased(BigDecimal original, BigDecimal adjusted, String details) {
            return new PriceAdjustment(
                    "DEMAND_BASED",
                    details,
                    original,
                    adjusted,
                    adjusted.subtract(original));
        }

        public static PriceAdjustment seasonal(BigDecimal original, BigDecimal adjusted, String season) {
            return new PriceAdjustment(
                    "SEASONAL",
                    "Seasonal adjustment: " + season,
                    original,
                    adjusted,
                    adjusted.subtract(original));
        }

        public static PriceAdjustment promotional(BigDecimal original, BigDecimal adjusted, String code) {
            return new PriceAdjustment(
                    "PROMOTIONAL",
                    "Promo code: " + code,
                    original,
                    adjusted,
                    adjusted.subtract(original));
        }
    }

    /**
     * Builder for creating pricing previews.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BigDecimal basePrice;
        private BigDecimal demandAdjustedPrice;
        private BigDecimal seasonalPrice;
        private BigDecimal finalPrice;
        private List<PriceAdjustment> breakdown = new ArrayList<>();

        public Builder basePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder demandAdjustedPrice(BigDecimal demandAdjustedPrice) {
            this.demandAdjustedPrice = demandAdjustedPrice;
            return this;
        }

        public Builder seasonalPrice(BigDecimal seasonalPrice) {
            this.seasonalPrice = seasonalPrice;
            return this;
        }

        public Builder finalPrice(BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
            return this;
        }

        public Builder addAdjustment(PriceAdjustment adjustment) {
            this.breakdown.add(adjustment);
            return this;
        }

        public Builder breakdown(List<PriceAdjustment> breakdown) {
            this.breakdown = new ArrayList<>(breakdown);
            return this;
        }

        public PricingPreview build() {
            // Set defaults if not provided
            if (demandAdjustedPrice == null) {
                demandAdjustedPrice = basePrice;
            }
            if (seasonalPrice == null) {
                seasonalPrice = demandAdjustedPrice;
            }
            if (finalPrice == null) {
                finalPrice = seasonalPrice;
            }

            return new PricingPreview(
                    basePrice,
                    demandAdjustedPrice,
                    seasonalPrice,
                    finalPrice,
                    breakdown,
                    LocalDateTime.now());
        }
    }
}
