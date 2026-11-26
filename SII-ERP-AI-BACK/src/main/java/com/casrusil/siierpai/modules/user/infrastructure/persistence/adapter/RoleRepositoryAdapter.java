package com.casrusil.siierpai.modules.user.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.user.domain.model.*;
import com.casrusil.siierpai.modules.user.domain.port.out.RoleRepository;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.RoleEntity;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.repository.RoleJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Set<Permission>> PERMISSION_SET = new TypeReference<>() {
    };


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
        entity.setPermissions(writePermissions(role.getPermissions()));
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
                .permissions(readPermissions(entity.getPermissions()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Serializa los permisos a JSON para la columna {@code roles.permissions}
     * (jsonb). Antes siempre se guardaba {@code "[]"} y los permisos del rol se
     * perdían al persistir.
     */
    static String writePermissions(Set<Permission> permissions) {
        try {
            return JSON.writeValueAsString(permissions == null ? Set.of() : permissions);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudieron serializar los permisos del rol", e);
        }
    }

    static Set<Permission> readPermissions(String json) {
        if (json == null || json.isBlank()) {
            return new HashSet<>();
        }
        try {
            return new HashSet<>(JSON.readValue(json, PERMISSION_SET));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Permisos del rol con formato inválido", e);
        }
    }
}
