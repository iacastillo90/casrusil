package com.casrusil.siierpai.modules.pos.domain.event;

import com.casrusil.siierpai.modules.pos.domain.model.PosTransaction;
import com.casrusil.siierpai.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class PosTransactionCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final PosTransaction transaction;

    public PosTransactionCompletedEvent(PosTransaction transaction) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.transaction = transaction;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    public PosTransaction getTransaction() {
        return transaction;
    }
}
