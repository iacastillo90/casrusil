package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModel;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModelId;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ManufacturerId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargerModelRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ChargerModelEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.ChargerModelJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChargerModelRepositoryAdapter implements ChargerModelRepository {

    private final ChargerModelJpaRepository jpaRepository;

    @Override
    public ChargerModel save(ChargerModel model) {
        ChargerModelEntity entity = mapToEntity(model);
        ChargerModelEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<ChargerModel> findById(ChargerModelId id) {
        return jpaRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    public List<ChargerModel> findByManufacturerId(ManufacturerId manufacturerId) {
        return jpaRepository.findAll().stream()
                .filter(e -> e.getManufacturerId().equals(manufacturerId.getValue()))
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargerModel> findAllActive() {
        return jpaRepository.findAll().stream()
                .filter(ChargerModelEntity::isActive)
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargerModel> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargerModel> searchByModelName(String searchTerm) {
        String lower = searchTerm.toLowerCase();
        return jpaRepository.findAll().stream()
                .filter(e -> e.getModelName().toLowerCase().contains(lower) || 
                             (e.getModelNumber() != null && e.getModelNumber().toLowerCase().contains(lower)))
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargerModel> findByOcppVersion(String ocppVersion) {
        return jpaRepository.findAll().stream()
                .filter(e -> ocppVersion.equals(e.getOcppVersion()))
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(ChargerModelId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean exists(ChargerModelId id) {
        return jpaRepository.existsById(id.getValue());
    }

    private ChargerModelEntity mapToEntity(ChargerModel domain) {
        return new ChargerModelEntity(
                domain.getId().getValue(),
                domain.getManufacturerId().getValue(),
                domain.getModelName(),
                domain.getModelNumber(),
                domain.getNumberOfConnectors(),
                domain.getMaxPowerKw(),
                domain.getFirmwareVersion(),
                domain.getOcppVersion(),
                domain.supportsSmartCharging(),
                domain.supportsReservation(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private ChargerModel mapToDomain(ChargerModelEntity entity) {
        return ChargerModel.builder()
                .id(ChargerModelId.of(entity.getId()))
                .manufacturerId(ManufacturerId.of(entity.getManufacturerId()))
                .modelName(entity.getModelName())
                .modelNumber(entity.getModelNumber())
                .numberOfConnectors(entity.getNumberOfConnectors())
                .maxPowerKw(entity.getMaxPowerKw())
                .firmwareVersion(entity.getFirmwareVersion())
                .ocppVersion(entity.getOcppVersion())
                .supportsSmartCharging(entity.isSupportsSmartCharging())
                .supportsReservation(entity.isSupportsReservation())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
