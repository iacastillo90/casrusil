package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ChargerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository: Cargadores.
 * Proporciona acceso a datos para ChargerEntity.
 */
@Repository
public interface ChargerJpaRepository extends JpaRepository<ChargerEntity, Long> {

    /**
     * Busca un cargador por su OCPP ID único.
     */
    Optional<ChargerEntity> findByOcppId(String ocppId);

    /**
     * Verifica si existe un cargador con el OCPP ID dado.
     */
    boolean existsByOcppId(String ocppId);
}
