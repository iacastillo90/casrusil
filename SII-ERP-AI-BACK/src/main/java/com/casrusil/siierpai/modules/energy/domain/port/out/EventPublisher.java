package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.event.DomainEvent;

/**
 * Puerto de Salida: Publicador de Eventos de Dominio.
 * Permite al dominio emitir eventos sin conocer la implementación (Spring
 * Events, Kafka, RabbitMQ, etc.).
 * 
 * Patrón: Hexagonal Architecture - Output Port
 * Implementación: En infrastructure/adapter/out/event
 */
public interface EventPublisher {

    /**
     * Publica un evento de dominio de forma asíncrona.
     * El evento será procesado por todos los listeners suscritos.
     */
    void publish(DomainEvent event);

    /**
     * Publica múltiples eventos en una transacción.
     * Todos los eventos se publican o ninguno (atomicidad).
     */
    void publishAll(DomainEvent... events);
}
