package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.DiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating promotional campaign.
 */
public record CreateCampaignRequest(
        @NotBlank(message = "Code is required") @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters") String code,

        @NotBlank(message = "Description is required") String description,

        @NotNull(message = "Discount type is required") DiscountType discountType,

        @NotNull(message = "Discount value is required") @Positive(message = "Discount value must be positive") BigDecimal discountValue,

        @NotNull(message = "Valid from date is required") LocalDateTime validFrom,

        @NotNull(message = "Valid until date is required") LocalDateTime validUntil,

        @Positive(message = "Max usage count must be positive if specified") Integer maxUsageCount, // Optional

        @PositiveOrZero(message = "Minimum purchase must be non-negative") BigDecimal minimumPurchase, // Optional

        @PositiveOrZero(message = "Maximum discount must be non-negative") BigDecimal maximumDiscount, // Optional

        Set<UUID> eligibleUserIds // Optional
) {
}
