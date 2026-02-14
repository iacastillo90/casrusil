package com.casrusil.siierpai.modules.energy.domain.event;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Sesión de Carga Iniciada.
 * Se emite cuando un usuario inicia una sesión de carga en un conector.
 * CRÍTICO: El módulo de contabilidad debe escuchar este evento para crear el
 * asiento inicial.
 */
public final class ChargingSessionStartedEvent implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final SessionId sessionId;
    private final StationId stationId;
    private final OcppIdentifier chargerOcppId;
    private final ConnectorId connectorId;
    private final UUID userId;
    private final EnergyMeasurement initialMeasurement;

    public ChargingSessionStartedEvent(
            SessionId sessionId,
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId,
            UUID userId,
            EnergyMeasurement initialMeasurement) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.sessionId = Objects.requireNonNull(sessionId);
        this.stationId = Objects.requireNonNull(stationId);
        this.chargerOcppId = Objects.requireNonNull(chargerOcppId);
        this.connectorId = Objects.requireNonNull(connectorId);
        this.userId = Objects.requireNonNull(userId);
        this.initialMeasurement = Objects.requireNonNull(initialMeasurement);
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

    @Override
    public String toString() {
        return String.format("ChargingSessionStartedEvent[sessionId=%s, stationId=%s, userId=%s, occurredOn=%s]",
                sessionId, stationId, userId, occurredOn);
    }
}
