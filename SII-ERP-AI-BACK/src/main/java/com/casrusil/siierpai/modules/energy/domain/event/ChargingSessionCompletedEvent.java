package com.casrusil.siierpai.modules.energy.domain.event;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Sesión de Carga Completada.
 * Se emite cuando una sesión de carga finaliza exitosamente.
 * CRÍTICO: El módulo de contabilidad debe escuchar este evento para:
 * - Registrar el ingreso por venta de energía
 * - Calcular el costo de la energía consumida
 * - Generar el asiento contable de cierre
 */
public final class ChargingSessionCompletedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final SessionId sessionId;
    private final StationId stationId;
    private final OcppIdentifier chargerOcppId;
    private final ConnectorId connectorId;
    private final UUID userId;
    private final EnergyMeasurement initialMeasurement;
    private final EnergyMeasurement finalMeasurement;
    private final BigDecimal totalEnergyKwh;
    private final Duration sessionDuration;
    private final String stopReason;
    
    public ChargingSessionCompletedEvent(
            SessionId sessionId,
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId,
            UUID userId,
            EnergyMeasurement initialMeasurement,
            EnergyMeasurement finalMeasurement,
            BigDecimal totalEnergyKwh,
            Duration sessionDuration,
            String stopReason
    ) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.sessionId = Objects.requireNonNull(sessionId);
        this.stationId = Objects.requireNonNull(stationId);
        this.chargerOcppId = Objects.requireNonNull(chargerOcppId);
        this.connectorId = Objects.requireNonNull(connectorId);
        this.userId = Objects.requireNonNull(userId);
        this.initialMeasurement = Objects.requireNonNull(initialMeasurement);
        this.finalMeasurement = Objects.requireNonNull(finalMeasurement);
        this.totalEnergyKwh = Objects.requireNonNull(totalEnergyKwh);
        this.sessionDuration = Objects.requireNonNull(sessionDuration);
        this.stopReason = stopReason;
    }
    
    @Override
    public UUID getEventId() {
        return eventId;
    }
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    @Override
    public UUID getAggregateId() {
        return sessionId.getValue();
    }
    
    public SessionId getSessionId() {
        return sessionId;
    }
    
    public StationId getStationId() {
        return stationId;
    }
    
    public OcppIdentifier getChargerOcppId() {
        return chargerOcppId;
    }
    
    public ConnectorId getConnectorId() {
        return connectorId;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public EnergyMeasurement getInitialMeasurement() {
        return initialMeasurement;
    }
    
    public EnergyMeasurement getFinalMeasurement() {
        return finalMeasurement;
    }
    
    public BigDecimal getTotalEnergyKwh() {
        return totalEnergyKwh;
    }
    
    public Duration getSessionDuration() {
        return sessionDuration;
    }
    
    public String getStopReason() {
        return stopReason;
    }
    
    @Override
    public String toString() {
        return String.format("ChargingSessionCompletedEvent[sessionId=%s, totalEnergy=%.2f kWh, duration=%s, occurredOn=%s]",
                sessionId, totalEnergyKwh, sessionDuration, occurredOn);
    }
}
