package com.casrusil.siierpai.modules.energy.domain.model.pricing;

import java.util.UUID;

/**
 * Value object representing a unique identifier for promotional campaigns.
 */
public record CampaignId(UUID value) {

    public static CampaignId generate() {
        return new CampaignId(UUID.randomUUID());
    }

    public static CampaignId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CampaignId value cannot be null");
        }
        return new CampaignId(value);
    }
}
