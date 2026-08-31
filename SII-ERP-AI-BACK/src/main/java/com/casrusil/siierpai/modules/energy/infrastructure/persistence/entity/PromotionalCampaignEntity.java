package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotional_campaigns", schema = "energy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionalCampaignEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "max_usage_count")
    private Integer maxUsageCount;

    @Column(name = "current_usage_count", nullable = false)
    private Integer currentUsageCount;

    @Column(name = "eligible_user_ids", columnDefinition = "text")
    private String eligibleUserIds; // Comma separated UUIDs

    @Column(name = "minimum_purchase", precision = 10, scale = 2)
    private BigDecimal minimumPurchase;

    @Column(name = "maximum_discount", precision = 10, scale = 2)
    private BigDecimal maximumDiscount;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
