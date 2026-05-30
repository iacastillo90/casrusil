package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.DiscountType;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PromotionalCampaign;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for promotional campaign.
 */
public record PromotionalCampaignDto(
        UUID id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        Integer maxUsageCount,
        Integer currentUsageCount,
        Integer remainingUsageCount,
        BigDecimal minimumPurchase,
        BigDecimal maximumDiscount,
        Set<UUID> eligibleUserIds,
        boolean active) {
    public static PromotionalCampaignDto from(PromotionalCampaign campaign) {
        return new PromotionalCampaignDto(
                campaign.getId().value(),
                campaign.getCode(),
                campaign.getDescription(),
                campaign.getType(),
                campaign.getDiscountValue(),
                campaign.getValidFrom(),
                campaign.getValidUntil(),
                campaign.getMaxUsageCount(),
                campaign.getCurrentUsageCount(),
                campaign.getRemainingUsageCount(),
                campaign.getMinimumPurchase(),
                campaign.getMaximumDiscount(),
                campaign.getEligibleUserIds(),
                campaign.isActive());
    }
}
