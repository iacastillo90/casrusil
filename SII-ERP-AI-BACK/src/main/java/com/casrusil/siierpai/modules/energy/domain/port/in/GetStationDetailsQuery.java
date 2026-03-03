package com.casrusil.siierpai.modules.energy.domain.port.in;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Entrada: Query - Obtener Detalles de Estación.
 * Permite consultar información detallada de una estación de carga.
 * 
 * Patrón: CQRS - Query Side
 */
public interface GetStationDetailsQuery {

    /**
     * Obtiene los detalles completos de una estación.
     */
    Optional<StationDetails> execute(StationId stationId);

    /**
     * DTO con detalles de la estación.
     */
    record StationDetails(
            StationId stationId,
            String name,
            StationLocation location,
            StationStatus status,
            boolean active,
            int totalConnectors,
            long availableConnectors,
            long activeChargingSessions,
            double totalCapacityKw,
            boolean supportsFastCharging,
            List<ChargerInfo> chargers) {
    }

    record ChargerInfo(
            OcppIdentifier ocppId,
            String name,
            ChargerStatus status,
            List<ConnectorInfo> connectors) {
    }

    record ConnectorInfo(
            ConnectorId connectorId,
            ConnectorType type,
            ConnectorStatus status,
            double maxPowerKw) {
    }
}
