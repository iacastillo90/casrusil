package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.CampaignId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.DiscountType;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PromotionalCampaign;
import com.casrusil.siierpai.modules.energy.domain.port.out.PromotionalCampaignRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.PromotionalCampaignEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.PromotionalCampaignJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PromotionalCampaignRepositoryAdapter implements PromotionalCampaignRepository {

    private final PromotionalCampaignJpaRepository jpaRepository;

    @Override
    public PromotionalCampaign save(PromotionalCampaign campaign) {
        PromotionalCampaignEntity entity = mapToEntity(campaign);
        PromotionalCampaignEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<PromotionalCampaign> findById(CampaignId id) {
        return jpaRepository.findById(id.value()).map(this::mapToDomain);
    }

    @Override
    public Optional<PromotionalCampaign> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::mapToDomain);
    }

    @Override
    public List<PromotionalCampaign> findActiveAt(LocalDateTime dateTime) {
        return jpaRepository.findAll().stream()
                .filter(PromotionalCampaignEntity::getActive)
                .filter(e -> !dateTime.isBefore(e.getValidFrom()) && !dateTime.isAfter(e.getValidUntil()))
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionalCampaign> findEligibleForUser(UUID userId, LocalDateTime dateTime) {
        return jpaRepository.findAll().stream()
                .filter(PromotionalCampaignEntity::getActive)
                .filter(e -> !dateTime.isBefore(e.getValidFrom()) && !dateTime.isAfter(e.getValidUntil()))
                .filter(e -> {
                    if (e.getEligibleUserIds() == null || e.getEligibleUserIds().isEmpty()) return true;
                    return Arrays.asList(e.getEligibleUserIds().split(",")).contains(userId.toString());
                })
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionalCampaign> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(CampaignId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.findByCode(code).isPresent();
    }

    private PromotionalCampaignEntity mapToEntity(PromotionalCampaign domain) {
        String users = domain.getEligibleUserIds() != null && !domain.getEligibleUserIds().isEmpty()
            ? domain.getEligibleUserIds().stream().map(UUID::toString).collect(Collectors.joining(","))
            : "";
        return PromotionalCampaignEntity.builder()
                .id(domain.getId().value())
                .code(domain.getCode())
                .description(domain.getDescription())
                .type(domain.getType().name())
                .discountValue(domain.getDiscountValue())
                .validFrom(domain.getValidFrom())
                .validUntil(domain.getValidUntil())
                .maxUsageCount(domain.getMaxUsageCount())
                .currentUsageCount(domain.getCurrentUsageCount())
                .eligibleUserIds(users)
                .minimumPurchase(domain.getMinimumPurchase())
                .maximumDiscount(domain.getMaximumDiscount())
                .active(domain.isActive())
                .build();
    }

    private PromotionalCampaign mapToDomain(PromotionalCampaignEntity entity) {
        Set<UUID> users = new HashSet<>();
        if (entity.getEligibleUserIds() != null && !entity.getEligibleUserIds().isEmpty()) {
            users = Arrays.stream(entity.getEligibleUserIds().split(","))
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        }
        return PromotionalCampaign.builder()
                .id(new CampaignId(entity.getId()))
                .code(entity.getCode())
                .description(entity.getDescription())
                .type(DiscountType.valueOf(entity.getType()))
                .discountValue(entity.getDiscountValue())
                .validFrom(entity.getValidFrom())
                .validUntil(entity.getValidUntil())
                .maxUsageCount(entity.getMaxUsageCount())
                .currentUsageCount(entity.getCurrentUsageCount())
                .eligibleUserIds(users)
                .minimumPurchase(entity.getMinimumPurchase())
                .maximumDiscount(entity.getMaximumDiscount())
                .active(entity.getActive())
                .build();
    }
}
