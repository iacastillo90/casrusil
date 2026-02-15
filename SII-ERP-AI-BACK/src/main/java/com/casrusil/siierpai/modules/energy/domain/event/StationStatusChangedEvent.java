package com.casrusil.siierpai.modules.energy.domain.event;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.StationStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Estado de Estación Cambiado.
 * Se emite cuando una estación de carga cambia su estado operacional.
 */
public final class StationStatusChangedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final StationId stationId;
    private final StationStatus previousStatus;
    private final StationStatus newStatus;
    private final String reason;
    
    public StationStatusChangedEvent(
            StationId stationId,
            StationStatus previousStatus,
            StationStatus newStatus,
            String reason
    ) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.stationId = Objects.requireNonNull(stationId);
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus);
        this.reason = reason;
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
    
    public StationStatus getPreviousStatus() {
        return previousStatus;
    }
    
    public StationStatus getNewStatus() {
        return newStatus;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return String.format("StationStatusChangedEvent[stationId=%s, %s -> %s, reason=%s]",
                stationId, previousStatus, newStatus, reason);
    }
}
