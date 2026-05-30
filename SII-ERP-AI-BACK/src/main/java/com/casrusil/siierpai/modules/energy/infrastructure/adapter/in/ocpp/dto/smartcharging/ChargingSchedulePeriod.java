package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

/**
 * OCPP 1.6: ChargingSchedulePeriod.
 * Período de tiempo con límite de potencia.
 */
public record ChargingSchedulePeriod(
        int startPeriod, // Offset en segundos desde startSchedule
        double limit, // Límite de potencia (W o A según chargingRateUnit)
        Integer numberPhases // Número de fases (1 o 3)
) {
}
