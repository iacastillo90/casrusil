package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.pricing.CampaignId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PromotionalCampaign;
import com.casrusil.siierpai.modules.energy.domain.port.out.PromotionalCampaignRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of PromotionalCampaignRepository for testing.
 * TODO: Replace with JPA implementation for production.
 */
@Component
public class InMemoryPromotionalCampaignRepository implements PromotionalCampaignRepository {

    private final Map<CampaignId, PromotionalCampaign> storage = new ConcurrentHashMap<>();

    @Override
    public PromotionalCampaign save(PromotionalCampaign campaign) {
        storage.put(campaign.getId(), campaign);
        return campaign;
    }

    @Override
    public Optional<PromotionalCampaign> findById(CampaignId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<PromotionalCampaign> findByCode(String code) {
        return storage.values().stream()
                .filter(campaign -> campaign.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public List<PromotionalCampaign> findActiveAt(LocalDateTime dateTime) {
        return storage.values().stream()
                .filter(PromotionalCampaign::isActive)
                .filter(campaign -> !dateTime.isBefore(campaign.getValidFrom()) &&
                        !dateTime.isAfter(campaign.getValidUntil()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionalCampaign> findEligibleForUser(UUID userId, LocalDateTime dateTime) {
        return findActiveAt(dateTime).stream()
                .filter(campaign -> campaign.isValid(dateTime, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionalCampaign> findAll() {
        return storage.values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(CampaignId id) {
        storage.remove(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return storage.values().stream()
                .anyMatch(campaign -> campaign.getCode().equalsIgnoreCase(code));
    }

    // Testing utility methods
    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
