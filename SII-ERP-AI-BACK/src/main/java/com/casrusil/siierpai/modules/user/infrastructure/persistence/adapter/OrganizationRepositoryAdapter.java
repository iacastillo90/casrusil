package com.casrusil.siierpai.modules.user.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.user.domain.model.*;
import com.casrusil.siierpai.modules.user.domain.port.out.OrganizationRepository;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.entity.OrganizationEntity;
import com.casrusil.siierpai.modules.user.infrastructure.persistence.repository.OrganizationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrganizationRepositoryAdapter implements OrganizationRepository {

    private final OrganizationJpaRepository jpaRepository;

    public OrganizationRepositoryAdapter(OrganizationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Organization save(Organization organization) {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId(organization.getId().value());
        entity.setName(organization.getName());
        entity.setType(OrganizationEntity.OrganizationType.valueOf(organization.getType().name()));
        entity.setStatus(OrganizationEntity.OrganizationStatus.valueOf(organization.getStatus().name()));
        entity.setParentId(organization.getParentId() != null ? organization.getParentId().value() : null);
        entity.setSettings("{}");
        entity.setBranding("{}");
        entity.setCreatedAt(organization.getCreatedAt());
        entity.setUpdatedAt(organization.getUpdatedAt());
        
        OrganizationEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Organization> findById(OrganizationId id) {
        return jpaRepository.findById(id.value()).map(this::mapToDomain);
    }

    @Override
    public List<Organization> findAll() {
        return jpaRepository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Organization> findActive() {
        return jpaRepository.findByStatus(OrganizationEntity.OrganizationStatus.ACTIVE)
                .stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Organization> findByParentId(OrganizationId parentId) {
        return jpaRepository.findByParentId(parentId.value())
                .stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private Organization mapToDomain(OrganizationEntity entity) {
        return Organization.builder()
                .id(OrganizationId.of(entity.getId()))
                .name(entity.getName())
                .type(OrganizationType.valueOf(entity.getType().name()))
                .status(OrganizationStatus.valueOf(entity.getStatus().name()))
                .parentId(entity.getParentId() != null ? OrganizationId.of(entity.getParentId()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
