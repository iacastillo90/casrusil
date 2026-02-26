package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique pricing rule identifier.
 */
public class PricingRuleId {
    private final UUID value;

    private PricingRuleId(UUID value) {
        this.value = Objects.requireNonNull(value, "Pricing rule ID cannot be null");
    }

    public static PricingRuleId of(UUID value) {
        return new PricingRuleId(value);
    }

    public static PricingRuleId generate() {
        return new PricingRuleId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PricingRuleId that = (PricingRuleId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
