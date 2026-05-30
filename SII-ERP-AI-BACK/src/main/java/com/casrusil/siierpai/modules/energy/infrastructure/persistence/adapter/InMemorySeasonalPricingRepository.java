package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.SeasonalPricing;
import com.casrusil.siierpai.modules.energy.domain.port.out.SeasonalPricingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory implementation of SeasonalPricingRepository for testing.
 * TODO: Replace with JPA implementation for production.
 */
@Component
public class InMemorySeasonalPricingRepository implements SeasonalPricingRepository {

    private final List<SeasonalPricing> storage = new CopyOnWriteArrayList<>();

    @Override
    public SeasonalPricing save(SeasonalPricing pricing) {
        // Remove existing entry with same season and start date
        storage.removeIf(existing -> existing.season().equals(pricing.season()) &&
                existing.startDate().equals(pricing.startDate()));
        storage.add(pricing);
        return pricing;
    }

    @Override
    public List<SeasonalPricing> findActiveOn(LocalDate date) {
        return storage.stream()
                .filter(pricing -> pricing.isActive(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<SeasonalPricing> findBySeason(SeasonalPricing.Season season) {
        return storage.stream()
                .filter(pricing -> pricing.season().equals(season))
                .collect(Collectors.toList());
    }

    @Override
    public List<SeasonalPricing> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public void delete(SeasonalPricing.Season season, LocalDate startDate) {
        storage.removeIf(pricing -> pricing.season().equals(season) &&
                pricing.startDate().equals(startDate));
    }

    // Testing utility methods
    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
