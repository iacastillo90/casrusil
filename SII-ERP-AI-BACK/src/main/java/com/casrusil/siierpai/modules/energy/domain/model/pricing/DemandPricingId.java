package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.util.UUID;

/**
 * Value object representing a unique identifier for demand-based pricing rules.
 */
public record DemandPricingId(UUID value) {

    public static DemandPricingId generate() {
        return new DemandPricingId(UUID.randomUUID());
    }

    public static DemandPricingId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("DemandPricingId value cannot be null");
        }
        return new DemandPricingId(value);
    }
}
