package com.casrusil.siierpai.modules.user.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for Organization entities.
 */
@Repository
public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, UUID> {

    /**
     * Finds organizations by status.
     */
    List<OrganizationEntity> findByStatus(OrganizationEntity.OrganizationStatus status);

    /**
     * Finds child organizations.
     */
    List<OrganizationEntity> findByParentId(UUID parentId);

    /**
     * Finds organizations by type.
     */
    List<OrganizationEntity> findByType(OrganizationEntity.OrganizationType type);

    /**
     * Checks if organization exists by name.
     */
    boolean existsByName(String name);

    /**
     * Finds all active organizations.
     */
    @Query("SELECT o FROM OrganizationEntity o WHERE o.status = 'ACTIVE'")
    List<OrganizationEntity> findAllActive();
}
