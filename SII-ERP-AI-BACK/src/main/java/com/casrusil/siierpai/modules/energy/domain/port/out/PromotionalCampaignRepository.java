package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.CampaignId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PromotionalCampaign;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for promotional campaigns.
 */
public interface PromotionalCampaignRepository {

    /**
     * Saves a promotional campaign.
     */
    PromotionalCampaign save(PromotionalCampaign campaign);

    /**
     * Finds a campaign by ID.
     */
    Optional<PromotionalCampaign> findById(CampaignId id);

    /**
     * Finds a campaign by its code.
     */
    Optional<PromotionalCampaign> findByCode(String code);

    /**
     * Finds all active campaigns valid at a specific time.
     */
    List<PromotionalCampaign> findActiveAt(LocalDateTime dateTime);

    /**
     * Finds all campaigns eligible for a specific user.
     */
    List<PromotionalCampaign> findEligibleForUser(UUID userId, LocalDateTime dateTime);

    /**
     * Finds all campaigns.
     */
    List<PromotionalCampaign> findAll();

    /**
     * Deletes a campaign.
     */
    void delete(CampaignId id);

    /**
     * Checks if a campaign code exists.
     */
    boolean existsByCode(String code);
}
