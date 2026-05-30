package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

import java.util.List;

/**
 * OCPP 1.6: ChargingSchedule.
 * Horario de carga con períodos de potencia.
 */
public record ChargingSchedule(
        Integer duration,
        String startSchedule,
        String chargingRateUnit, // "W" | "A"
        List<ChargingSchedulePeriod> chargingSchedulePeriod,
        Double minChargingRate) {
}
