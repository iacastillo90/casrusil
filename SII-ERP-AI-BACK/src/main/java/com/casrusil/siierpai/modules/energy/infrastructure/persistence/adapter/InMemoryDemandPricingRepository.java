package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.DemandBasedPricing;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.DemandPricingId;
import com.casrusil.siierpai.modules.energy.domain.port.out.DemandPricingRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of DemandPricingRepository for testing.
 * TODO: Replace with JPA implementation for production.
 */
@Component
public class InMemoryDemandPricingRepository implements DemandPricingRepository {

    private final Map<DemandPricingId, DemandBasedPricing> storage = new ConcurrentHashMap<>();

    @Override
    public DemandBasedPricing save(DemandBasedPricing pricing) {
        storage.put(pricing.getId(), pricing);
        return pricing;
    }

    @Override
    public Optional<DemandBasedPricing> findById(DemandPricingId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<DemandBasedPricing> findAllActive() {
        return storage.values().stream()
                .filter(DemandBasedPricing::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandBasedPricing> findByApplicableDay(DayOfWeek dayOfWeek) {
        return storage.values().stream()
                .filter(DemandBasedPricing::isActive)
                .filter(pricing -> pricing.getApplicableDays() == null ||
                        pricing.getApplicableDays().isEmpty() ||
                        pricing.getApplicableDays().contains(dayOfWeek))
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandBasedPricing> findByStationId(StationId stationId) {
        // For now, return all active rules (station-specific filtering not implemented)
        return findAllActive();
    }

    @Override
    public void delete(DemandPricingId id) {
        storage.remove(id);
    }

    @Override
    public boolean exists(DemandPricingId id) {
        return storage.containsKey(id);
    }

    // Testing utility methods
    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
