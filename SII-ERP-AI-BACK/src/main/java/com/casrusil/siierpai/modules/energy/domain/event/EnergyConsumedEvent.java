package com.casrusil.siierpai.modules.energy.domain.event;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Energía Consumida.
 * Se emite periódicamente durante una sesión de carga activa (cada MeterValues OCPP).
 * CRÍTICO: El módulo de contabilidad debe escuchar este evento para:
 * - Actualizar el consumo acumulado en tiempo real
 * - Calcular costos parciales
 * - Generar reportes de consumo en vivo
 */
public final class EnergyConsumedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final SessionId sessionId;
    private final StationId stationId;
    private final OcppIdentifier chargerOcppId;
    private final ConnectorId connectorId;
    private final EnergyMeasurement measurement;
    private final BigDecimal cumulativeEnergyKwh; // Energía acumulada desde el inicio
    
    public EnergyConsumedEvent(
            SessionId sessionId,
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId,
            EnergyMeasurement measurement,
            BigDecimal cumulativeEnergyKwh
    ) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.sessionId = Objects.requireNonNull(sessionId);
        this.stationId = Objects.requireNonNull(stationId);
        this.chargerOcppId = Objects.requireNonNull(chargerOcppId);
        this.connectorId = Objects.requireNonNull(connectorId);
        this.measurement = Objects.requireNonNull(measurement);
        this.cumulativeEnergyKwh = Objects.requireNonNull(cumulativeEnergyKwh);
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
    
    public EnergyMeasurement getMeasurement() {
        return measurement;
    }
    
    public BigDecimal getCumulativeEnergyKwh() {
        return cumulativeEnergyKwh;
    }
    
    @Override
    public String toString() {
        return String.format("EnergyConsumedEvent[sessionId=%s, measurement=%s, cumulative=%.2f kWh]",
                sessionId, measurement, cumulativeEnergyKwh);
    }
}
