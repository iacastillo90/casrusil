package com.casrusil.siierpai.modules.energy.domain.model.monitoring;

import java.util.UUID;

/**
 * Value object representing a unique identifier for dashboard metrics.
 */
public record MetricId(UUID value) {

    public static MetricId generate() {
        return new MetricId(UUID.randomUUID());
    }

    public static MetricId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("MetricId value cannot be null");
        }
        return new MetricId(value);
    }
}
