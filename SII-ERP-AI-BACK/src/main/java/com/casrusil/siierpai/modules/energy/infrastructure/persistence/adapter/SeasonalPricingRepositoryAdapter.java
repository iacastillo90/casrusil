package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.SeasonalPricing;
import com.casrusil.siierpai.modules.energy.domain.port.out.SeasonalPricingRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.SeasonalPricingEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.SeasonalPricingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SeasonalPricingRepositoryAdapter implements SeasonalPricingRepository {

    private final SeasonalPricingJpaRepository jpaRepository;

    @Override
    public SeasonalPricing save(SeasonalPricing pricing) {
        SeasonalPricingEntity entity = mapToEntity(pricing);
        SeasonalPricingEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public List<SeasonalPricing> findActiveOn(LocalDate date) {
        return jpaRepository.findAll().stream()
                .filter(e -> !date.isBefore(e.getStartDate()) && !date.isAfter(e.getEndDate()))
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeasonalPricing> findBySeason(SeasonalPricing.Season season) {
        return jpaRepository.findAll().stream()
                .filter(e -> e.getSeason().equals(season.name()))
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeasonalPricing> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(SeasonalPricing.Season season, LocalDate startDate) {
        jpaRepository.findAll().stream()
                .filter(e -> e.getSeason().equals(season.name()) && e.getStartDate().equals(startDate))
                .findFirst()
                .ifPresent(e -> jpaRepository.delete(e));
    }

    private SeasonalPricingEntity mapToEntity(SeasonalPricing domain) {
        return SeasonalPricingEntity.builder()
                .season(domain.season().name())
                .multiplier(domain.multiplier())
                .startDate(domain.startDate())
                .endDate(domain.endDate())
                .description(domain.description())
                .build();
    }

    private SeasonalPricing mapToDomain(SeasonalPricingEntity entity) {
        return new SeasonalPricing(
                SeasonalPricing.Season.valueOf(entity.getSeason()),
                entity.getMultiplier(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDescription()
        );
    }
}
