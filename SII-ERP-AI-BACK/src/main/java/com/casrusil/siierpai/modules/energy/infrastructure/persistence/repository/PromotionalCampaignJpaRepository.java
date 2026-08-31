package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.PromotionalCampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface PromotionalCampaignJpaRepository extends JpaRepository<PromotionalCampaignEntity, UUID> {
    Optional<PromotionalCampaignEntity> findByCode(String code);
}
