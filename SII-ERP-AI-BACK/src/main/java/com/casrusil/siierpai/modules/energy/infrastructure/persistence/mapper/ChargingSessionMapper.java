package com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper;

import com.casrusil.siierpai.modules.energy.domain.model.*;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper: Conversión bidireccional entre ChargingSession (dominio) y
 * ChargingSessionEntity (JPA).
 */
@Component
public class ChargingSessionMapper {

    // ========== DOMAIN → JPA ==========

    public ChargingSessionEntity toEntity(ChargingSession domain) {
        if (domain == null) {
            return null;
        }

        ChargingSessionEntity entity = new ChargingSessionEntity(
                domain.getSessionId().getValue(),
                domain.getStationId().getValue(),
                domain.getChargerOcppId().getValue(),
                domain.getConnectorId().getValue(),
                domain.getUserId(),
                domain.getStatus(),
                domain.getStartedAt());

        entity.setEndedAt(domain.getEndedAt());
        entity.setStopReason(domain.getStopReason());

        // Mapear mediciones inicial y final
        if (domain.getInitialMeasurement() != null) {
            entity.setInitialEnergyKwh(domain.getInitialMeasurement().getEnergyKwh());
        }
        if (domain.getFinalMeasurement() != null) {
            entity.setFinalEnergyKwh(domain.getFinalMeasurement().getEnergyKwh());
        }

        // Mapear mediciones intermedias
        domain.getIntermediateMeasurements().forEach(measurement -> {
            EnergyMeasurementEntity measurementEntity = toMeasurementEntity(measurement);
            entity.addMeasurement(measurementEntity);
        });

        return entity;
    }

    public EnergyMeasurementEntity toMeasurementEntity(EnergyMeasurement domain) {
        if (domain == null) {
            return null;
        }

        return new EnergyMeasurementEntity(
                domain.getEnergyKwh(),
                domain.getMeasuredAt(),
                domain.getMeterValue());
    }

    // ========== JPA → DOMAIN ==========

    public ChargingSession toDomain(ChargingSessionEntity entity) {
        if (entity == null) {
            return null;
        }

        EnergyMeasurement initialMeasurement = entity.getInitialEnergyKwh() != null
                ? EnergyMeasurement.of(entity.getInitialEnergyKwh(), entity.getStartedAt())
                : null;
        EnergyMeasurement lastMeasurement = entity.getFinalEnergyKwh() != null && entity.getEndedAt() != null
                ? EnergyMeasurement.of(entity.getFinalEnergyKwh(), entity.getEndedAt())
                : null;

        // Rehidratación: no se re-ejecutan startCharging/complete (fallarían para
        // sesiones ya iniciadas o completadas y pisarían endedAt con "ahora").
        ChargingSession session = ChargingSession.restore(
                SessionId.of(entity.getId()),
                StationId.of(entity.getStationId()),
                OcppIdentifier.of(entity.getChargerOcppId()),
                ConnectorId.of(entity.getConnectorNumber()),
                entity.getUserId(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                entity.getStatus(),
                initialMeasurement,
                lastMeasurement);

        entity.getMeasurements().forEach(measurementEntity ->
                session.addRestoredMeasurement(toMeasurementDomain(measurementEntity)));

        return session;
    }

    public EnergyMeasurement toMeasurementDomain(EnergyMeasurementEntity entity) {
        if (entity == null) {
            return null;
        }

        return EnergyMeasurement.of(
                entity.getEnergyKwh(),
                entity.getMeasuredAt(),
                entity.getMeterValue());
    }
}
