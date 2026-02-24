package com.casrusil.siierpai.modules.energy.domain.model.monitoring;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Value object representing a snapshot of a metric at a specific point in time.
 */
public record MetricSnapshot(
        String metricName,
        BigDecimal value,
        String unit,
        LocalDateTime timestamp) {
    public MetricSnapshot {
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be null or blank");
        }
        if (value == null) {
            throw new IllegalArgumentException("Metric value cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
    }

    public static MetricSnapshot of(String metricName, BigDecimal value, String unit) {
        return new MetricSnapshot(metricName, value, unit, LocalDateTime.now());
    }

    public static MetricSnapshot of(String metricName, double value, String unit) {
        return new MetricSnapshot(metricName, BigDecimal.valueOf(value), unit, LocalDateTime.now());
    }

    public static MetricSnapshot of(String metricName, int value, String unit) {
        return new MetricSnapshot(metricName, BigDecimal.valueOf(value), unit, LocalDateTime.now());
    }
}
