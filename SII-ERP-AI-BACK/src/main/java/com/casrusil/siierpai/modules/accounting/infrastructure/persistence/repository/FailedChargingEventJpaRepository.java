package com.casrusil.siierpai.modules.accounting.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.FailedChargingEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de la Dead Letter Queue de eventos de carga fallidos.
 */
public interface FailedChargingEventJpaRepository extends JpaRepository<FailedChargingEventEntity, UUID> {

    /** Eventos pendientes de reprocesar (no resueltos). */
    List<FailedChargingEventEntity> findByResolvedFalse();
}
