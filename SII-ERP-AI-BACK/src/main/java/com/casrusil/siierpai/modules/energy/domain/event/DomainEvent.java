package com.casrusil.siierpai.modules.energy.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interfaz base para todos los Eventos de Dominio.
 * Los eventos representan hechos que ya ocurrieron en el dominio.
 * Son inmutables y se nombran en pasado.
 */
public interface DomainEvent {

    /**
     * Identificador único del evento.
     */
    UUID getEventId();

    /**
     * Timestamp de cuando ocurrió el evento.
     */
    LocalDateTime getOccurredOn();

    /**
     * Tipo de evento (nombre de la clase).
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }

    /**
     * ID del agregado que generó el evento.
     */
    UUID getAggregateId();
}
