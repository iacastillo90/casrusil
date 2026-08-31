package com.casrusil.siierpai.modules.user.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.user.domain.model.*;
import com.casrusil.siierpai.modules.user.domain.port.out.RoleRepository;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.RoleEntity;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.repository.RoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpaRepository;

    public RoleRepositoryAdapter(RoleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId().value());
        entity.setOrganizationId(role.getOrganizationId() != null ? role.getOrganizationId().value() : null);
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setSystemRole(role.isSystemRole());
        entity.setPermissions("[]"); // TODO: map permissions properly (JSON string)
        entity.setCreatedAt(role.getCreatedAt());
        entity.setUpdatedAt(role.getUpdatedAt());
        
        RoleEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Role> findById(RoleId id) {
        return jpaRepository.findById(id.value()).map(this::mapToDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Role> findSystemRoles() {
        return jpaRepository.findAllSystemRoles().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Role> findByOrganizationId(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationId(organizationId.value()).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private Role mapToDomain(RoleEntity entity) {
        return Role.builder()
                .id(RoleId.of(entity.getId()))
                .organizationId(entity.getOrganizationId() != null ? OrganizationId.of(entity.getOrganizationId()) : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .systemRole(entity.isSystemRole())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
