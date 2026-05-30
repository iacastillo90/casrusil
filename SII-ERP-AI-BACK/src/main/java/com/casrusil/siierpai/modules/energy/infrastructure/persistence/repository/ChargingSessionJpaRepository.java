package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSessionStatus;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ChargingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository: Sesiones de Carga.
 * Proporciona acceso a datos para ChargingSessionEntity.
 */
@Repository
public interface ChargingSessionJpaRepository extends JpaRepository<ChargingSessionEntity, UUID> {

    /**
     * Busca la sesión activa en un conector específico.
     */
    @Query("""
            SELECT s FROM ChargingSessionEntity s
            WHERE s.stationId = :stationId
            AND s.chargerOcppId = :chargerOcppId
            AND s.connectorNumber = :connectorNumber
            AND s.status IN ('INITIATED', 'CHARGING', 'SUSPENDED')
            """)
    Optional<ChargingSessionEntity> findActiveSessionByConnector(
            UUID stationId,
            String chargerOcppId,
            Integer connectorNumber);

    /**
     * Busca todas las sesiones activas de una estación.
     */
    @Query("""
            SELECT s FROM ChargingSessionEntity s
            WHERE s.stationId = :stationId
            AND s.status IN ('INITIATED', 'CHARGING', 'SUSPENDED')
            """)
    List<ChargingSessionEntity> findActiveSessionsByStation(UUID stationId);

    /**
     * Busca todas las sesiones de un usuario.
     */
    List<ChargingSessionEntity> findByUserIdOrderByStartedAtDesc(UUID userId);

    /**
     * Busca sesiones completadas en un rango de fechas.
     */
    List<ChargingSessionEntity> findByStatusAndStartedAtBetween(
            ChargingSessionStatus status,
            LocalDateTime start,
            LocalDateTime end);

    /**
     * Busca sesiones por estación en un rango de fechas.
     */
    List<ChargingSessionEntity> findByStationIdAndStartedAtBetween(
            UUID stationId,
            LocalDateTime start,
            LocalDateTime end);

    /**
     * Cuenta sesiones activas en una estación.
     */
    @Query("""
            SELECT COUNT(s) FROM ChargingSessionEntity s
            WHERE s.stationId = :stationId
            AND s.status IN ('INITIATED', 'CHARGING', 'SUSPENDED')
            """)
    long countActiveSessionsByStation(UUID stationId);

    /**
     * Verifica si existe una sesión activa en un conector.
     */
    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM ChargingSessionEntity s
            WHERE s.stationId = :stationId
            AND s.chargerOcppId = :chargerOcppId
            AND s.connectorNumber = :connectorNumber
            AND s.status IN ('INITIATED', 'CHARGING', 'SUSPENDED')
            """)
    boolean hasActiveSession(UUID stationId, String chargerOcppId, Integer connectorNumber);
}
