package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.monitoring.Alert;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertSeverity;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record EnergyAlertDto(
        String id,
        String stationId,
        EnergyAlertType type,
        EnergyAlertSeverity severity,
        String message,
        String timestamp,
        boolean resolved
) {
    public static EnergyAlertDto from(Alert alert) {
        return new EnergyAlertDto(
                alert.getId().value().toString(),
                alert.getStationId() != null ? alert.getStationId().getValue().toString() : null,
                mapAlertType(alert.getType()),
                mapSeverity(alert.getSeverity()),
                alert.getMessage(),
                alert.getCreatedAt().format(DateTimeFormatter.ISO_INSTANT),
                alert.getResolvedAt() != null
        );
    }

    private static EnergyAlertType mapAlertType(com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertType type) {
        if (type == null) return EnergyAlertType.CONNECTIVITY;
        String typeName = type.name().toUpperCase();
        
        if (typeName.contains("OFFLINE") || typeName.contains("ERROR") || 
            typeName.contains("API") || typeName.contains("DB") || typeName.contains("SYSTEM")) {
            return EnergyAlertType.CONNECTIVITY;
        }
        if (typeName.contains("HIGH_DEMAND") || typeName.contains("PEAK_LOAD") || 
            typeName.contains("CAPACITY_WARNING") || typeName.contains("POWER_SPIKE")) {
            return EnergyAlertType.OVERLOAD;
        }
        if (typeName.contains("SESSION_TIMEOUT") || typeName.contains("SESSION_ERROR") || 
            typeName.contains("LONG_SESSION") || typeName.contains("HIGH_ENERGY") || typeName.contains("LOW_EFF")) {
            return EnergyAlertType.TEMPERATURE;
        }
        if (typeName.contains("PRICING_ANOMALY") || typeName.contains("LOW_REVENUE")) {
            return EnergyAlertType.CONNECTIVITY;
        }
        if (typeName.contains("MAINTENANCE") || typeName.contains("VOLTAGE")) {
            return EnergyAlertType.VOLTAGE_DROP;
        }
        return EnergyAlertType.CONNECTIVITY;
    }

    private static EnergyAlertSeverity mapSeverity(AlertSeverity severity) {
        if (severity == null) return EnergyAlertSeverity.LOW;
        return switch (severity) {
            case CRITICAL -> EnergyAlertSeverity.HIGH;
            case WARNING -> EnergyAlertSeverity.MEDIUM;
            default -> EnergyAlertSeverity.LOW;
        };
    }

    public enum EnergyAlertType {
        VOLTAGE_DROP, OVERLOAD, TEMPERATURE, CONNECTIVITY
    }

    public enum EnergyAlertSeverity {
        LOW, MEDIUM, HIGH
    }
}