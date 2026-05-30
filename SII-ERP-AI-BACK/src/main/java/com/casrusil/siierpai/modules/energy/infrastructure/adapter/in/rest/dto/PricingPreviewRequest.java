package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for pricing preview.
 */
public record PricingPreviewRequest(
        @NotNull(message = "Station ID is required") UUID stationId,

        @NotNull(message = "Estimated kWh is required") @Positive(message = "Estimated kWh must be positive") BigDecimal estimatedKwh,

        LocalDateTime startTime, // Optional, defaults to now

        String promoCode, // Optional

        UUID userId, // Optional

        @PositiveOrZero(message = "Current demand must be non-negative") Integer currentDemandKw // Optional
) {
}
