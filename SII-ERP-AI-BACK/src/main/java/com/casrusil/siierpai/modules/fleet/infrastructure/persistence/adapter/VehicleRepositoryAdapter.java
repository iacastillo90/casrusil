package com.casrusil.siierpai.modules.fleet.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.fleet.domain.model.Vehicle;
import com.casrusil.siierpai.modules.fleet.domain.model.VehicleStatus;
import com.casrusil.siierpai.modules.fleet.domain.port.out.VehicleRepository;
import com.casrusil.siierpai.modules.fleet.infrastructure.persistence.entity.VehicleEntity;
import com.casrusil.siierpai.modules.fleet.infrastructure.persistence.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VehicleRepositoryAdapter implements VehicleRepository {

    private final VehicleJpaRepository jpaRepository;

    @Override
    public List<Vehicle> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = mapToEntity(vehicle);
        VehicleEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<Vehicle> findByRfidTag(String rfidTag) {
        return jpaRepository.findByRfidTag(rfidTag).map(this::mapToDomain);
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return jpaRepository.findByLicensePlate(licensePlate).map(this::mapToDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    private VehicleEntity mapToEntity(Vehicle domain) {
        return VehicleEntity.builder()
                .id(domain.getId())
                .licensePlate(domain.getLicensePlate())
                .brand(domain.getBrand())
                .model(domain.getModel())
                .year(domain.getYear())
                .rfidTag(domain.getRfidTag())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .batteryLevel(domain.getBatteryLevel())
                .currentChargingStationId(domain.getCurrentChargingStationId())
                .lastChargingSession(domain.getLastChargingSession())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private Vehicle mapToDomain(VehicleEntity entity) {
        return Vehicle.builder()
                .id(entity.getId())
                .licensePlate(entity.getLicensePlate())
                .brand(entity.getBrand())
                .model(entity.getModel())
                .year(entity.getYear())
                .rfidTag(entity.getRfidTag())
                .status(entity.getStatus() != null ? VehicleStatus.valueOf(entity.getStatus()) : null)
                .batteryLevel(entity.getBatteryLevel())
                .currentChargingStationId(entity.getCurrentChargingStationId())
                .lastChargingSession(entity.getLastChargingSession())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
