package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.DemandBasedPricing;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.DemandPricingId;
import com.casrusil.siierpai.modules.energy.domain.port.out.DemandPricingRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.DemandPricingEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.DemandPricingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DemandPricingRepositoryAdapter implements DemandPricingRepository {

    private final DemandPricingJpaRepository jpaRepository;

    @Override
    public DemandBasedPricing save(DemandBasedPricing pricing) {
        DemandPricingEntity entity = mapToEntity(pricing);
        DemandPricingEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<DemandBasedPricing> findById(DemandPricingId id) {
        return jpaRepository.findById(id.value()).map(this::mapToDomain);
    }

    @Override
    public List<DemandBasedPricing> findAllActive() {
        return jpaRepository.findAll().stream()
                .filter(DemandPricingEntity::getActive)
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandBasedPricing> findByApplicableDay(DayOfWeek dayOfWeek) {
        return jpaRepository.findAll().stream()
                .filter(DemandPricingEntity::getActive)
                .filter(e -> {
                    if (e.getApplicableDays() == null || e.getApplicableDays().isEmpty()) return true;
                    return Arrays.asList(e.getApplicableDays().split(",")).contains(dayOfWeek.name());
                })
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandBasedPricing> findByStationId(StationId stationId) {
        return findAllActive();
    }

    @Override
    public void delete(DemandPricingId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean exists(DemandPricingId id) {
        return jpaRepository.existsById(id.value());
    }

    private DemandPricingEntity mapToEntity(DemandBasedPricing domain) {
        String days = domain.getApplicableDays() != null && !domain.getApplicableDays().isEmpty() 
            ? domain.getApplicableDays().stream().map(DayOfWeek::name).collect(Collectors.joining(","))
            : "";
        return DemandPricingEntity.builder()
                .id(domain.getId().value())
                .name(domain.getName())
                .description(domain.getDescription())
                .baseRate(domain.getBaseRate())
                .peakMultiplier(domain.getPeakMultiplier())
                .offPeakMultiplier(domain.getOffPeakMultiplier())
                .demandThresholdKw(domain.getDemandThresholdKw())
                .peakStartTime(domain.getPeakStartTime())
                .peakEndTime(domain.getPeakEndTime())
                .applicableDays(days)
                .active(domain.isActive())
                .build();
    }

    private DemandBasedPricing mapToDomain(DemandPricingEntity entity) {
        Set<DayOfWeek> days = new HashSet<>();
        if (entity.getApplicableDays() != null && !entity.getApplicableDays().isEmpty()) {
            days = Arrays.stream(entity.getApplicableDays().split(","))
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toSet());
        }
        return DemandBasedPricing.builder()
                .id(new DemandPricingId(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .baseRate(entity.getBaseRate())
                .peakMultiplier(entity.getPeakMultiplier())
                .offPeakMultiplier(entity.getOffPeakMultiplier())
                .demandThresholdKw(entity.getDemandThresholdKw())
                .peakStartTime(entity.getPeakStartTime())
                .peakEndTime(entity.getPeakEndTime())
                .applicableDays(days)
                .active(entity.getActive())
                .build();
    }
}
