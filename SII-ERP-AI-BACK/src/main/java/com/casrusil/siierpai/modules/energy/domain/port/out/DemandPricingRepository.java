package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.DemandBasedPricing;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.DemandPricingId;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for demand-based pricing rules.
 */
public interface DemandPricingRepository {

    /**
     * Saves a demand pricing rule.
     */
    DemandBasedPricing save(DemandBasedPricing pricing);

    /**
     * Finds a demand pricing rule by ID.
     */
    Optional<DemandBasedPricing> findById(DemandPricingId id);

    /**
     * Finds all active demand pricing rules.
     */
    List<DemandBasedPricing> findAllActive();

    /**
     * Finds demand pricing rules applicable for a specific day of week.
     */
    List<DemandBasedPricing> findByApplicableDay(DayOfWeek dayOfWeek);

    /**
     * Finds demand pricing rules for a specific station.
     * (If station-specific pricing is implemented)
     */
    List<DemandBasedPricing> findByStationId(StationId stationId);

    /**
     * Deletes a demand pricing rule.
     */
    void delete(DemandPricingId id);

    /**
     * Checks if a demand pricing rule exists.
     */
    boolean exists(DemandPricingId id);
}
