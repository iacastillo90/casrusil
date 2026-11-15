package com.casrusil.siierpai.modules.procurement.domain.event;

import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrder;
import com.casrusil.siierpai.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class PurchaseOrderReceivedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final Instant occurredOn;
    private final PurchaseOrder purchaseOrder;

    public PurchaseOrderReceivedEvent(PurchaseOrder purchaseOrder) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.purchaseOrder = purchaseOrder;
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }
}
