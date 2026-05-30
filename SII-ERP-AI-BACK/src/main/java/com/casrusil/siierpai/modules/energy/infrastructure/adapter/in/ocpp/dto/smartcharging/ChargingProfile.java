package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

import java.util.List;

/**
 * OCPP 1.6: ChargingProfile.
 * Define un perfil de carga con horarios y límites de potencia.
 */
public record ChargingProfile(
        int chargingProfileId,
        Integer transactionId,
        int stackLevel,
        String chargingProfilePurpose, // "ChargePointMaxProfile" | "TxDefaultProfile" | "TxProfile"
        String chargingProfileKind, // "Absolute" | "Recurring" | "Relative"
        String recurrencyKind, // "Daily" | "Weekly" (si kind=Recurring)
        String validFrom,
        String validTo,
        ChargingSchedule chargingSchedule) {
}
