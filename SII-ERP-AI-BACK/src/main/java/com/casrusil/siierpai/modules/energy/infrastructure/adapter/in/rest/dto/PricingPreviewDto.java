package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingPreview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for pricing preview.
 */
public record PricingPreviewDto(
        BigDecimal basePrice,
        BigDecimal demandAdjustedPrice,
        BigDecimal seasonalPrice,
        BigDecimal finalPrice,
        BigDecimal totalDiscount,
        BigDecimal discountPercentage,
        List<PriceAdjustmentDto> breakdown,
        LocalDateTime calculatedAt) {
    public static PricingPreviewDto from(PricingPreview preview) {
        return new PricingPreviewDto(
                preview.basePrice(),
                preview.demandAdjustedPrice(),
                preview.seasonalPrice(),
                preview.finalPrice(),
                preview.getTotalDiscount(),
                preview.getDiscountPercentage(),
                preview.breakdown().stream()
                        .map(PriceAdjustmentDto::from)
                        .toList(),
                preview.calculatedAt());
    }

    public record PriceAdjustmentDto(
            String type,
            String description,
            BigDecimal originalAmount,
            BigDecimal adjustedAmount,
            BigDecimal difference) {
        public static PriceAdjustmentDto from(PricingPreview.PriceAdjustment adjustment) {
            return new PriceAdjustmentDto(
                    adjustment.type(),
                    adjustment.description(),
                    adjustment.originalAmount(),
                    adjustment.adjustedAmount(),
                    adjustment.difference());
        }
    }
}
