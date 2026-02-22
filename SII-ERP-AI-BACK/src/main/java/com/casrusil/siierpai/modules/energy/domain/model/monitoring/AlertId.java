package com.casrusil.siierpai.modules.energy.domain.model.monitoring;

import java.util.UUID;

/**
 * Value object representing a unique identifier for alerts.
 */
public record AlertId(UUID value) {

    public static AlertId generate() {
        return new AlertId(UUID.randomUUID());
    }

    public static AlertId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("AlertId value cannot be null");
        }
        return new AlertId(value);
    }
}
