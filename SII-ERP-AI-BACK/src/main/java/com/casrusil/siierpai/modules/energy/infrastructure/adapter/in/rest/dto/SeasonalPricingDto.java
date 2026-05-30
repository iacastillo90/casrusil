package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.SeasonalPricing;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for seasonal pricing.
 */
public record SeasonalPricingDto(
        SeasonalPricing.Season season,
        BigDecimal multiplier,
        LocalDate startDate,
        LocalDate endDate,
        String description) {
    public static SeasonalPricingDto from(SeasonalPricing pricing) {
        return new SeasonalPricingDto(
                pricing.season(),
                pricing.multiplier(),
                pricing.startDate(),
                pricing.endDate(),
                pricing.description());
    }
}
