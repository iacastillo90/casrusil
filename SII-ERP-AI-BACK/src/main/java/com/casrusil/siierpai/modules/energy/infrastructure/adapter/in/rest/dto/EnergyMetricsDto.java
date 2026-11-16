package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record EnergyMetricsDto(
        double totalConsumptionKW,
        double estimatedDailyCost,
        double peakConsumptionKW,
        int totalStations,
        int activeStations,
        double averageLoadPercent
) {
    public static final double PRICE_PER_KWH = 150.0;

    public static EnergyMetricsDto from(
            double totalConsumptionKWh,
            int totalStations,
            int activeStations,
            int activeSessions
    ) {
        double cost = totalConsumptionKWh * PRICE_PER_KWH;
        double loadPercent = totalStations > 0 ? Math.min(100.0, (activeSessions * 100.0) / totalStations) : 0.0;
        
        return new EnergyMetricsDto(
                totalConsumptionKWh,
                cost,
                0.0,
                totalStations,
                activeStations,
                loadPercent
        );
    }
}