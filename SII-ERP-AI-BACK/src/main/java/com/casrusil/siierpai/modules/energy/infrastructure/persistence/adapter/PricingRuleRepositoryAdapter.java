package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingRule;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingRuleId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingType;
import com.casrusil.siierpai.modules.energy.domain.port.out.PricingRuleRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.PricingRuleEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.PricingRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Adapter for PricingRuleRepository.
 * 
 * <p>
 * Implements the hexagonal architecture port by adapting between
 * the domain model and JPA entities.
 * 
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class PricingRuleRepositoryAdapter implements PricingRuleRepository {

    private final PricingRuleJpaRepository jpaRepository;

    @Override
    public PricingRule save(PricingRule rule) {
        PricingRuleEntity entity = toEntity(rule);
        PricingRuleEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PricingRule> findById(PricingRuleId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }

    @Override
    public List<PricingRule> findAllActive() {
        return jpaRepository.findAllActive().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingRule> findByType(PricingType type) {
        return jpaRepository.findByType(type.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingRule> findActiveByType(PricingType type) {
        return jpaRepository.findActiveByType(type.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(PricingRuleId id) {
        jpaRepository.deleteById(id.getValue());
    }

    // Mapping methods
    private PricingRuleEntity toEntity(PricingRule rule) {
        return PricingRuleEntity.builder()
                .id(rule.getId().getValue())
                .type(rule.getType().name())
                .baseRate(rule.getBasePrice())
                .peakMultiplier(rule.getMultiplier())
                .offPeakMultiplier(rule.getMultiplier())
                .timeWindows(rule.getTimeSlot() != null ? rule.getTimeSlot().toString() : null)
                .active(rule.isActive())
                .createdAt(rule.getCreatedAt())
                .build();
    }

    private PricingRule toDomain(PricingRuleEntity entity) {
        return PricingRule.builder()
                .id(PricingRuleId.of(entity.getId()))
                .name("Rule-" + entity.getId().toString().substring(0, 8))
                .type(PricingType.valueOf(entity.getType()))
                .basePrice(entity.getBaseRate())
                .multiplier(entity.getPeakMultiplier())
                .timeSlot(null) // Simplified for now
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
