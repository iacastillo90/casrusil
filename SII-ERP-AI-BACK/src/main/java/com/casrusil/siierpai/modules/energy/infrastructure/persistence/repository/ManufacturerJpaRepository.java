package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ManufacturerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ManufacturerJpaRepository extends JpaRepository<ManufacturerEntity, UUID> {
    Optional<ManufacturerEntity> findByName(String name);
}
