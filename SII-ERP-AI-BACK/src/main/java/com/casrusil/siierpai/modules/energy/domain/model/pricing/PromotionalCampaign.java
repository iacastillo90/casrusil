package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate root for promotional pricing campaigns.
 * Manages discount codes, usage limits, and eligibility rules.
 */
public class PromotionalCampaign {

    private final CampaignId id;
    private final String code;
    private final String description;
    private final DiscountType type;
    private final BigDecimal discountValue;
    private final LocalDateTime validFrom;
    private final LocalDateTime validUntil;
    private final Integer maxUsageCount;
    private Integer currentUsageCount;
    private final Set<UUID> eligibleUserIds;
    private final BigDecimal minimumPurchase;
    private final BigDecimal maximumDiscount;
    private boolean active;

    private PromotionalCampaign(Builder builder) {
        this.id = builder.id;
        this.code = builder.code;
        this.description = builder.description;
        this.type = builder.type;
        this.discountValue = builder.discountValue;
        this.validFrom = builder.validFrom;
        this.validUntil = builder.validUntil;
        this.maxUsageCount = builder.maxUsageCount;
        this.currentUsageCount = builder.currentUsageCount;
        this.eligibleUserIds = builder.eligibleUserIds != null ? new HashSet<>(builder.eligibleUserIds)
                : new HashSet<>();
        this.minimumPurchase = builder.minimumPurchase;
        this.maximumDiscount = builder.maximumDiscount;
        this.active = builder.active;

        validate();
    }

    private void validate() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Campaign code cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Discount type cannot be null");
        }
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be positive");
        }
        if (type == DiscountType.PERCENTAGE && discountValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%");
        }
        if (validFrom == null || validUntil == null) {
            throw new IllegalArgumentException("Valid dates cannot be null");
        }
        if (validFrom.isAfter(validUntil)) {
            throw new IllegalArgumentException("Valid from must be before valid until");
        }
    }

    /**
     * Checks if the campaign is valid for the given timestamp and user.
     */
    public boolean isValid(LocalDateTime now, UUID userId) {
        if (!active) {
            return false;
        }

        // Check date validity
        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            return false;
        }

        // Check usage limit
        if (maxUsageCount != null && currentUsageCount >= maxUsageCount) {
            return false;
        }

        // Check user eligibility (if restricted)
        if (!eligibleUserIds.isEmpty() && !eligibleUserIds.contains(userId)) {
            return false;
        }

        return true;
    }

    /**
     * Applies the discount to the original price.
     */
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return originalPrice;
        }

        // Check minimum purchase requirement
        if (minimumPurchase != null && originalPrice.compareTo(minimumPurchase) < 0) {
            return originalPrice;
        }

        BigDecimal discountAmount = calculateDiscountAmount(originalPrice);

        // Apply maximum discount cap if set
        if (maximumDiscount != null && discountAmount.compareTo(maximumDiscount) > 0) {
            discountAmount = maximumDiscount;
        }

        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        // Ensure price doesn't go negative
        return finalPrice.max(BigDecimal.ZERO);
    }

    /**
     * Calculates the discount amount based on the discount type.
     */
    private BigDecimal calculateDiscountAmount(BigDecimal originalPrice) {
        return switch (type) {
            case PERCENTAGE -> originalPrice
                    .multiply(discountValue)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> discountValue;
            case FREE_KWH -> discountValue; // Handled differently in pricing service
        };
    }

    /**
     * Records a usage of this campaign.
     */
    public void recordUsage() {
        if (maxUsageCount != null && currentUsageCount >= maxUsageCount) {
            throw new IllegalStateException("Campaign usage limit reached");
        }
        currentUsageCount++;
    }

    /**
     * Activates the campaign.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivates the campaign.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Checks if the campaign has reached its usage limit.
     */
    public boolean hasReachedUsageLimit() {
        return maxUsageCount != null && currentUsageCount >= maxUsageCount;
    }

    /**
     * Gets the remaining usage count.
     */
    public Integer getRemainingUsageCount() {
        if (maxUsageCount == null) {
            return null; // Unlimited
        }
        return Math.max(0, maxUsageCount - currentUsageCount);
    }

    // Getters
    public CampaignId getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiscountType getType() {
        return type;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public Integer getMaxUsageCount() {
        return maxUsageCount;
    }

    public Integer getCurrentUsageCount() {
        return currentUsageCount;
    }

    public Set<UUID> getEligibleUserIds() {
        return new HashSet<>(eligibleUserIds);
    }

    public BigDecimal getMinimumPurchase() {
        return minimumPurchase;
    }

    public BigDecimal getMaximumDiscount() {
        return maximumDiscount;
    }

    public boolean isActive() {
        return active;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CampaignId id;
        private String code;
        private String description;
        private DiscountType type;
        private BigDecimal discountValue;
        private LocalDateTime validFrom;
        private LocalDateTime validUntil;
        private Integer maxUsageCount;
        private Integer currentUsageCount = 0;
        private Set<UUID> eligibleUserIds;
        private BigDecimal minimumPurchase;
        private BigDecimal maximumDiscount;
        private boolean active = true;

        public Builder id(CampaignId id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(DiscountType type) {
            this.type = type;
            return this;
        }

        public Builder discountValue(BigDecimal discountValue) {
            this.discountValue = discountValue;
            return this;
        }

        public Builder validFrom(LocalDateTime validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public Builder validUntil(LocalDateTime validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public Builder maxUsageCount(Integer maxUsageCount) {
            this.maxUsageCount = maxUsageCount;
            return this;
        }

        public Builder currentUsageCount(Integer currentUsageCount) {
            this.currentUsageCount = currentUsageCount;
            return this;
        }

        public Builder eligibleUserIds(Set<UUID> eligibleUserIds) {
            this.eligibleUserIds = eligibleUserIds;
            return this;
        }

        public Builder minimumPurchase(BigDecimal minimumPurchase) {
            this.minimumPurchase = minimumPurchase;
            return this;
        }

        public Builder maximumDiscount(BigDecimal maximumDiscount) {
            this.maximumDiscount = maximumDiscount;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public PromotionalCampaign build() {
            if (id == null) {
                id = CampaignId.generate();
            }
            return new PromotionalCampaign(this);
        }
    }
}
