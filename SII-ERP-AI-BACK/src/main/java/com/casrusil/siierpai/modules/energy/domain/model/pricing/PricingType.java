package com.casrusil.siierpai.modules.energy.domain.model.pricing;

/**
 * Pricing types for dynamic pricing engine.
 */
public enum PricingType {
    /**
     * Fixed price per kWh (default).
     */
    FIXED,

    /**
     * Time-of-use pricing with different rates for different hours.
     */
    TIME_OF_USE,

    /**
     * Peak and off-peak pricing.
     */
    PEAK_OFF_PEAK,

    /**
     * User-specific custom pricing.
     */
    USER_SPECIFIC,

    /**
     * Promotional pricing (discounts).
     */
    PROMOTIONAL
}
