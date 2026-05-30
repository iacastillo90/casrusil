package com.casrusil.siierpai.modules.energy.infrastructure.event;

import com.casrusil.siierpai.modules.energy.domain.event.DomainEvent;
import com.casrusil.siierpai.modules.energy.domain.port.out.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter: Implementación del puerto EventPublisher usando Spring
 * ApplicationEventPublisher.
 * 
 * Patrón: Hexagonal Architecture - Adapter (Output)
 * Responsabilidad: Publicar eventos de dominio usando la infraestructura de
 * Spring.
 * 
 * Los eventos publicados aquí serán escuchados por:
 * - Módulo de contabilidad (para EnergyConsumedEvent,
 * ChargingSessionCompletedEvent)
 * - Servicios de monitoreo
 * - Sistemas de auditoría
 */
@Component
public class EnergyEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher springPublisher;

    public EnergyEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        springPublisher.publishEvent(event);
    }

    @Override
    public void publishAll(DomainEvent... events) {
        for (DomainEvent event : events) {
            springPublisher.publishEvent(event);
        }
    }
}
