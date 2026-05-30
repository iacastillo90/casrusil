package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for current price.
 */
public record CurrentPriceDto(
        UUID stationId,
        BigDecimal pricePerKwh,
        LocalDateTime timestamp) {
}
