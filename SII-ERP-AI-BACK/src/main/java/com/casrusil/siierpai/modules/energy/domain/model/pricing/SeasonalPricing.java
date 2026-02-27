package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Value object for seasonal pricing adjustments.
 * Applies rate multipliers based on the season and date range.
 */
public record SeasonalPricing(
        Season season,
        BigDecimal multiplier,
        LocalDate startDate,
        LocalDate endDate,
        String description) {

    public SeasonalPricing {
        if (season == null) {
            throw new IllegalArgumentException("Season cannot be null");
        }
        if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    /**
     * Checks if this seasonal pricing is active for the given date.
     */
    public boolean isActive(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Applies the seasonal multiplier to the given price.
     */
    public BigDecimal applyTo(BigDecimal basePrice) {
        return basePrice.multiply(multiplier);
    }

    /**
     * Seasons for pricing adjustments.
     */
    public enum Season {
        SUMMER("Summer - High demand period"),
        WINTER("Winter - Moderate demand period"),
        SPRING("Spring - Low demand period"),
        FALL("Fall - Low demand period"),
        HOLIDAY("Holiday - Special rates");

        private final String description;

        Season(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Creates a summer seasonal pricing (typically higher rates).
     */
    public static SeasonalPricing summer(LocalDate start, LocalDate end, BigDecimal multiplier) {
        return new SeasonalPricing(
                Season.SUMMER,
                multiplier,
                start,
                end,
                "Summer high demand pricing");
    }

    /**
     * Creates a winter seasonal pricing.
     */
    public static SeasonalPricing winter(LocalDate start, LocalDate end, BigDecimal multiplier) {
        return new SeasonalPricing(
                Season.WINTER,
                multiplier,
                start,
                end,
                "Winter moderate demand pricing");
    }

    /**
     * Creates a holiday seasonal pricing.
     */
    public static SeasonalPricing holiday(LocalDate start, LocalDate end, BigDecimal multiplier, String description) {
        return new SeasonalPricing(
                Season.HOLIDAY,
                multiplier,
                start,
                end,
                description);
    }
}
