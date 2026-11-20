package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.Charger;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingStation;

import java.time.LocalDateTime;

/**
 * Estación de carga expuesta al front.
 *
 * <p>{@code currentLoadKW} se mantiene en {@code null}: el modelo OCPP del
 * backend solo guarda energía acumulada (kWh), no potencia instantánea. El
 * motivo se explica en {@code currentLoadNote}.</p>
 */
public record EnergyStationDto(
        String id,
        String name,
        EnergyStationType type,
        EnergyStationStatus status,
        boolean connected,
        double maxCapacityKW,
        Double currentLoadKW,
        Integer connectorCount,
        String currentLoadNote) {

    public static final String CURRENT_LOAD_UNAVAILABLE_REASON =
            "El medidor no reporta potencia instantánea: solo energía acumulada (kWh) por sesión.";

    public static EnergyStationDto from(ChargingStation station) {
        return new EnergyStationDto(
                station.getStationId().getValue().toString(),
                station.getName(),
                EnergyStationType.CHARGING,
                mapStatus(station.getStatus()),
                isConnected(station),
                station.getTotalChargingCapacityKw(),
                null,
                station.getTotalConnectors(),
                CURRENT_LOAD_UNAVAILABLE_REASON);
    }

    private static EnergyStationStatus mapStatus(com.casrusil.siierpai.modules.energy.domain.model.StationStatus status) {
        return switch (status) {
            case OPERATIONAL -> EnergyStationStatus.ACTIVE;
            case MAINTENANCE -> EnergyStationStatus.MAINTENANCE;
            case UNDER_CONSTRUCTION, DECOMMISSIONED -> EnergyStationStatus.OFFLINE;
        };
    }

    private static boolean isConnected(ChargingStation station) {
        if (!station.isActive()) {
            return false;
        }
        for (Charger charger : station.getChargers()) {
            LocalDateTime lastHeartbeat = charger.getLastHeartbeat();
            if (lastHeartbeat != null) {
                long minutesSinceHeartbeat = java.time.Duration.between(lastHeartbeat, LocalDateTime.now()).toMinutes();
                if (minutesSinceHeartbeat <= 10) {
                    return true;
                }
            }
            if (charger.isActive()) {
                return true;
            }
        }
        return false;
    }

    public enum EnergyStationType {
        CHARGING, GRID, SOLAR, BATTERY
    }

    public enum EnergyStationStatus {
        ACTIVE, MAINTENANCE, OFFLINE
    }
}
