package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * Request DTO for creating demand-based pricing rule.
 */
public record CreateDemandPricingRequest(
        @NotBlank(message = "Name is required") String name,

        String description,

        @NotNull(message = "Base rate is required") @PositiveOrZero(message = "Base rate must be non-negative") BigDecimal baseRate,

        @NotNull(message = "Peak multiplier is required") @Positive(message = "Peak multiplier must be positive") BigDecimal peakMultiplier,

        @NotNull(message = "Off-peak multiplier is required") @Positive(message = "Off-peak multiplier must be positive") BigDecimal offPeakMultiplier,

        @NotNull(message = "Demand threshold is required") @Positive(message = "Demand threshold must be positive") Integer demandThresholdKw,

        @NotNull(message = "Peak start time is required") LocalTime peakStartTime,

        @NotNull(message = "Peak end time is required") LocalTime peakEndTime,

        Set<DayOfWeek> applicableDays // Optional
) {
}
