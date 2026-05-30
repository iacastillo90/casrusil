package com.casrusil.siierpai.modules.user.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Role entities.
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {

    /**
     * Finds role by name.
     */
    Optional<RoleEntity> findByName(String name);

    /**
     * Finds all system roles.
     */
    List<RoleEntity> findBySystemRole(boolean systemRole);

    /**
     * Finds roles by organization.
     */
    List<RoleEntity> findByOrganizationId(UUID organizationId);

    /**
     * Finds roles by organization and system flag.
     */
    List<RoleEntity> findByOrganizationIdAndSystemRole(UUID organizationId, boolean systemRole);

    /**
     * Checks if role name exists for organization.
     */
    boolean existsByOrganizationIdAndName(UUID organizationId, String name);

    /**
     * Finds all system roles.
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.systemRole = true")
    List<RoleEntity> findAllSystemRoles();

    /**
     * Finds custom roles for organization.
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.organizationId = :orgId AND r.systemRole = false")
    List<RoleEntity> findCustomRolesByOrganization(UUID orgId);
}
