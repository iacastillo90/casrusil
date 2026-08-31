package com.casrusil.siierpai.modules.user.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.user.domain.model.*;
import com.casrusil.siierpai.modules.user.domain.port.out.UserRepository;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.UserEntity;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId().value());
        entity.setOrganizationId(user.getOrganizationId().value());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setPhone(user.getPhone());
        entity.setStatus(UserEntity.UserStatus.valueOf(user.getStatus().name()));
        entity.setEmailVerified(user.isEmailVerified());
        entity.setLastLoginAt(user.getLastLoginAt());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        // Map roles... (skipped for brevity, assuming cascade or handled separately)
        
        UserEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(this::mapToDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::mapToDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<User> findByOrganizationId(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationId(organizationId.value()).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private User mapToDomain(UserEntity entity) {
        return User.builder()
                .id(UserId.of(entity.getId()))
                .organizationId(OrganizationId.of(entity.getOrganizationId()))
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .status(UserStatus.valueOf(entity.getStatus().name()))
                .emailVerified(entity.isEmailVerified())
                .lastLoginAt(entity.getLastLoginAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
