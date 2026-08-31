package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida: Repositorio de Sesiones de Carga.
 * Define las operaciones de persistencia para sesiones de carga.
 * 
 * Patrón: Hexagonal Architecture - Output Port (Repository Pattern)
 * Implementación: En infrastructure/persistence
 */
public interface ChargingSessionRepository {

    /**
     * Guarda o actualiza una sesión de carga.
     */
    ChargingSession save(ChargingSession session);

    /**
     * Busca una sesión por su ID.
     */
    Optional<ChargingSession> findById(SessionId sessionId);

    /**
     * Busca la sesión activa en un conector específico.
     */
    Optional<ChargingSession> findActiveSessionByConnector(
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId);

    /**
     * Busca todas las sesiones activas de una estación.
     */
    List<ChargingSession> findActiveSessionsByStation(StationId stationId);

    /**
     * Busca todas las sesiones de un usuario.
     */
    List<ChargingSession> findByUserId(UUID userId);

    /**
     * Busca sesiones completadas en un rango de fechas.
     */
    List<ChargingSession> findCompletedBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Busca sesiones por estación en un rango de fechas.
     */
    List<ChargingSession> findByStationAndDateRange(
            StationId stationId,
            LocalDateTime start,
            LocalDateTime end);

    /**
     * Cuenta sesiones activas en una estación.
     */
    long countActiveSessionsByStation(StationId stationId);

    /**
     * Verifica si existe una sesión activa en un conector.
     */
    boolean hasActiveSession(StationId stationId, OcppIdentifier chargerOcppId, ConnectorId connectorId);
}
