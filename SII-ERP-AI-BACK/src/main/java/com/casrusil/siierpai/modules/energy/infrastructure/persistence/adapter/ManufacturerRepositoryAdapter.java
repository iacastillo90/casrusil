package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.metadata.Manufacturer;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ManufacturerId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ManufacturerRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ManufacturerEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.ManufacturerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ManufacturerRepositoryAdapter implements ManufacturerRepository {

    private final ManufacturerJpaRepository jpaRepository;

    @Override
    public Manufacturer save(Manufacturer manufacturer) {
        ManufacturerEntity entity = mapToEntity(manufacturer);
        ManufacturerEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Manufacturer> findById(ManufacturerId id) {
        return jpaRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    public Optional<Manufacturer> findByName(String name) {
        return jpaRepository.findByName(name).map(this::mapToDomain);
    }

    @Override
    public List<Manufacturer> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Manufacturer> findAllActive() {
        return jpaRepository.findAll().stream()
                .filter(ManufacturerEntity::isActive)
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(ManufacturerId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean exists(ManufacturerId id) {
        return jpaRepository.existsById(id.getValue());
    }

    private ManufacturerEntity mapToEntity(Manufacturer domain) {
        return new ManufacturerEntity(
                domain.getId().getValue(),
                domain.getName(),
                domain.getWebsite(),
                domain.getSupportEmail(),
                domain.getSupportPhone(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private Manufacturer mapToDomain(ManufacturerEntity entity) {
        return Manufacturer.builder()
                .id(ManufacturerId.of(entity.getId()))
                .name(entity.getName())
                .website(entity.getWebsite())
                .supportEmail(entity.getSupportEmail())
                .supportPhone(entity.getSupportPhone())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
