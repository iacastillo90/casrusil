package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.SeasonalPricingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SeasonalPricingJpaRepository extends JpaRepository<SeasonalPricingEntity, UUID> {
}
