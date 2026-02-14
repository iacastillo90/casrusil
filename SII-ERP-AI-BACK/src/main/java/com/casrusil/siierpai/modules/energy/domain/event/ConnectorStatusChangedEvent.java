package com.casrusil.siierpai.modules.energy.domain.event;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Estado de Conector Cambiado.
 * Se emite cuando un conector cambia su estado (OCPP StatusNotification).
 * Útil para monitoreo en tiempo real y análisis de disponibilidad.
 */
public final class ConnectorStatusChangedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final StationId stationId;
    private final OcppIdentifier chargerOcppId;
    private final ConnectorId connectorId;
    private final ConnectorStatus previousStatus;
    private final ConnectorStatus newStatus;
    
    public ConnectorStatusChangedEvent(
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId,
            ConnectorStatus previousStatus,
            ConnectorStatus newStatus
    ) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.stationId = Objects.requireNonNull(stationId);
        this.chargerOcppId = Objects.requireNonNull(chargerOcppId);
        this.connectorId = Objects.requireNonNull(connectorId);
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus);
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
        return stationId.getValue();
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
    
    public ConnectorStatus getPreviousStatus() {
        return previousStatus;
    }
    
    public ConnectorStatus getNewStatus() {
        return newStatus;
    }
    
    @Override
    public String toString() {
        return String.format("ConnectorStatusChangedEvent[stationId=%s, charger=%s, connector=%s, %s -> %s]",
                stationId, chargerOcppId, connectorId, previousStatus, newStatus);
    }
}
