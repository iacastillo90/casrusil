package com.casrusil.siierpai.modules.energy.domain.port.in;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.util.List;

/**
 * Puerto de Entrada: Query - Buscar Conectores Disponibles.
 * Permite buscar conectores disponibles para carga.
 * 
 * Patrón: CQRS - Query Side
 */
public interface FindAvailableConnectorsQuery {

    /**
     * Busca conectores disponibles en una estación específica.
     */
    List<AvailableConnector> execute(StationId stationId);

    /**
     * Busca conectores disponibles cerca de una ubicación.
     */
    List<AvailableConnector> executeNearby(Double latitude, Double longitude, Double radiusKm);

    /**
     * DTO con información de conector disponible.
     */
    record AvailableConnector(
            StationId stationId,
            String stationName,
            StationLocation location,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId,
            ConnectorType type,
            double maxPowerKw,
            boolean isFastCharging,
            Double distanceKm) {
    }
}
