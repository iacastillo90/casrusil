package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.SeasonalPricing;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating seasonal pricing.
 */
public record CreateSeasonalPricingRequest(
        @NotNull(message = "Season is required") SeasonalPricing.Season season,

        @NotNull(message = "Multiplier is required") @Positive(message = "Multiplier must be positive") BigDecimal multiplier,

        @NotNull(message = "Start date is required") LocalDate startDate,

        @NotNull(message = "End date is required") LocalDate endDate,

        String description) {
}
