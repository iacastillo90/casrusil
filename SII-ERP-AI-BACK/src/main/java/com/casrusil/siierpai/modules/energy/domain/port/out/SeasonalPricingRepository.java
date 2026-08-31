package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.SeasonalPricing;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository port for seasonal pricing rules.
 */
public interface SeasonalPricingRepository {

    /**
     * Saves a seasonal pricing rule.
     */
    SeasonalPricing save(SeasonalPricing pricing);

    /**
     * Finds all seasonal pricing rules active on a specific date.
     */
    List<SeasonalPricing> findActiveOn(LocalDate date);

    /**
     * Finds all seasonal pricing rules for a specific season.
     */
    List<SeasonalPricing> findBySeason(SeasonalPricing.Season season);

    /**
     * Finds all seasonal pricing rules.
     */
    List<SeasonalPricing> findAll();

    /**
     * Deletes a seasonal pricing rule.
     */
    void delete(SeasonalPricing.Season season, LocalDate startDate);
}
