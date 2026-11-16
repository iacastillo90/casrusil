package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ChargeSessionDto(
        String id,
        String stationId,
        String connectorId,
        String vehicleId,
        String startTime,
        String endTime,
        double energyConsumedKWH,
        double cost,
        ChargeSessionStatus status
) {
    public static final double PRICE_PER_KWH = 150.0;

    public static ChargeSessionDto from(ChargingSession session) {
        return new ChargeSessionDto(
                session.getSessionId().getValue().toString(),
                session.getStationId().getValue().toString(),
                session.getConnectorId().getValue().toString(),
                null,
                session.getStartedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                session.getEndedAt() != null ? session.getEndedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                session.getTotalEnergyConsumed().doubleValue(),
                session.getTotalEnergyConsumed().multiply(BigDecimal.valueOf(PRICE_PER_KWH)).doubleValue(),
                mapStatus(session.getStatus())
        );
    }

    private static ChargeSessionStatus mapStatus(ChargingSessionStatus status) {
        return switch (status) {
            case INITIATED, CHARGING, SUSPENDED -> ChargeSessionStatus.ACTIVE;
            case COMPLETED -> ChargeSessionStatus.COMPLETED;
            case FAILED -> ChargeSessionStatus.CANCELLED;
        };
    }

    public enum ChargeSessionStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}