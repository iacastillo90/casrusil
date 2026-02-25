package com.casrusil.siierpai.modules.energy.domain.model.pricing;

/**
 * Types of discounts that can be applied in promotional campaigns.
 */
public enum DiscountType {
    /**
     * Percentage discount (e.g., 20% off).
     */
    PERCENTAGE("Percentage discount"),

    /**
     * Fixed amount discount (e.g., $10 off).
     */
    FIXED_AMOUNT("Fixed amount discount"),

    /**
     * Free kWh (e.g., first 10 kWh free).
     */
    FREE_KWH("Free kilowatt-hours");

    private final String description;

    DiscountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
