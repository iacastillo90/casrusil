package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ChargerModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ChargerModelJpaRepository extends JpaRepository<ChargerModelEntity, UUID> {
    Optional<ChargerModelEntity> findByModelNumber(String modelNumber);
}
