package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingRule;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingRuleId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingType;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for PricingRule persistence.
 */
public interface PricingRuleRepository {

    /**
     * Saves a pricing rule.
     */
    PricingRule save(PricingRule rule);

    /**
     * Finds a pricing rule by its ID.
     */
    Optional<PricingRule> findById(PricingRuleId id);

    /**
     * Finds all active pricing rules.
     */
    List<PricingRule> findAllActive();

    /**
     * Finds all pricing rules of a specific type.
     */
    List<PricingRule> findByType(PricingType type);

    /**
     * Finds all active pricing rules of a specific type.
     */
    List<PricingRule> findActiveByType(PricingType type);

    /**
     * Deletes a pricing rule.
     */
    void delete(PricingRuleId id);
}
