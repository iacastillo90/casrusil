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

        ChargingSession session = ChargingSession.builder()
                .sessionId(SessionId.of(entity.getId()))
                .stationId(StationId.of(entity.getStationId()))
                .chargerOcppId(OcppIdentifier.of(entity.getChargerOcppId()))
                .connectorId(ConnectorId.of(entity.getConnectorNumber()))
                .userId(entity.getUserId())
                .startedAt(entity.getStartedAt())
                .status(entity.getStatus())
                .build();

        // Reconstruir mediciones inicial y final
        if (entity.getInitialEnergyKwh() != null) {
            EnergyMeasurement initialMeasurement = EnergyMeasurement.of(
                    entity.getInitialEnergyKwh(),
                    entity.getStartedAt());
            session.startCharging(initialMeasurement);
        }

        // Reconstruir mediciones intermedias
        entity.getMeasurements().forEach(measurementEntity -> {
            EnergyMeasurement measurement = toMeasurementDomain(measurementEntity);
            if (session.getStatus().isActive()) {
                session.recordEnergyMeasurement(measurement);
            }
        });

        // Reconstruir medición final si existe
        if (entity.getFinalEnergyKwh() != null && entity.getEndedAt() != null) {
            EnergyMeasurement finalMeasurement = EnergyMeasurement.of(
                    entity.getFinalEnergyKwh(),
                    entity.getEndedAt());
            if (session.getStatus() == ChargingSessionStatus.COMPLETED) {
                session.complete(finalMeasurement, entity.getStopReason());
            }
        }

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
