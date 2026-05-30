package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.DemandBasedPricing;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for demand-based pricing.
 */
public record DemandPricingDto(
        UUID id,
        String name,
        String description,
        BigDecimal baseRate,
        BigDecimal peakMultiplier,
        BigDecimal offPeakMultiplier,
        Integer demandThresholdKw,
        LocalTime peakStartTime,
        LocalTime peakEndTime,
        Set<DayOfWeek> applicableDays,
        boolean active) {
    public static DemandPricingDto from(DemandBasedPricing pricing) {
        return new DemandPricingDto(
                pricing.getId().value(),
                pricing.getName(),
                pricing.getDescription(),
                pricing.getBaseRate(),
                pricing.getPeakMultiplier(),
                pricing.getOffPeakMultiplier(),
                pricing.getDemandThresholdKw(),
                pricing.getPeakStartTime(),
                pricing.getPeakEndTime(),
                pricing.getApplicableDays(),
                pricing.isActive());
    }
}
