package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.PricingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for PricingRuleEntity.
 * 
 * <p>
 * Provides database access for pricing rule persistence with
 * custom queries for active rules and type filtering.
 * 
 * @since 1.0
 */
@Repository
public interface PricingRuleJpaRepository extends JpaRepository<PricingRuleEntity, UUID> {

    /**
     * Find all active pricing rules.
     */
    @Query("SELECT p FROM PricingRuleEntity p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<PricingRuleEntity> findAllActive();

    /**
     * Find pricing rules by type.
     */
    @Query("SELECT p FROM PricingRuleEntity p WHERE p.type = :type ORDER BY p.createdAt DESC")
    List<PricingRuleEntity> findByType(@Param("type") String type);

    /**
     * Find active pricing rules by type.
     */
    @Query("SELECT p FROM PricingRuleEntity p WHERE p.type = :type AND p.active = true ORDER BY p.createdAt DESC")
    List<PricingRuleEntity> findActiveByType(@Param("type") String type);
}
