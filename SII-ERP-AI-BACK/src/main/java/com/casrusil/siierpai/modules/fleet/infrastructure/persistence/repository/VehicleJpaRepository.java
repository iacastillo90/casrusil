package com.casrusil.siierpai.modules.fleet.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.fleet.infrastructure.persistence.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, UUID> {
    Optional<VehicleEntity> findByRfidTag(String rfidTag);
    Optional<VehicleEntity> findByLicensePlate(String licensePlate);
}
