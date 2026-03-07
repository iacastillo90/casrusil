package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

/**
 * OCPP 1.6: SampledValue.
 * Valor individual de una medición.
 */
public record SampledValue(
        String value,
        String context, // "Sample.Periodic" | "Transaction.Begin" | "Transaction.End" | ...
        String format, // "Raw" | "SignedData"
        String measurand, // "Energy.Active.Import.Register" | "Power.Active.Import" | "Current.Import" |
                          // "Voltage" | ...
        String phase, // "L1" | "L2" | "L3" | "N" | "L1-N" | ...
        String location, // "Inlet" | "Outlet" | "Body"
        String unit // "Wh" | "kWh" | "W" | "kW" | "A" | "V" | ...
) {
}
