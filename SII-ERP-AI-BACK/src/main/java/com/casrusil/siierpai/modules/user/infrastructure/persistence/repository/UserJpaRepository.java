package com.casrusil.siierpai.modules.user.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for User entities.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds user by email.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Finds users by organization.
     */
    List<UserEntity> findByOrganizationId(UUID organizationId);

    /**
     * Finds users by status.
     */
    List<UserEntity> findByStatus(UserEntity.UserStatus status);

    /**
     * Finds users by organization and status.
     */
    List<UserEntity> findByOrganizationIdAndStatus(UUID organizationId, UserEntity.UserStatus status);

    /**
     * Checks if email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Finds all active users.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE'")
    List<UserEntity> findAllActive();

    /**
     * Finds users with unverified emails.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.emailVerified = false")
    List<UserEntity> findUnverifiedUsers();

    /**
     * Finds users by role.
     */
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.id = :roleId")
    List<UserEntity> findByRoleId(@Param("roleId") UUID roleId);
}
