package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

/**
 * Métricas de energía del día para la empresa.
 *
 * @param totalConsumptionKW energía consumida hoy (kWh)
 * @param estimatedDailyCost costo estimado de esa energía
 * @param peakConsumptionKW  mayor potencia promedio de una sesión completada hoy;
 *                           {@code null} cuando no hay sesiones con duración medible
 * @param totalStations      estaciones vigentes
 * @param activeStations     estaciones operativas y activas
 * @param averageLoadPercent carga promedio según sesiones activas
 * @param peakConsumptionNote explica por qué {@code peakConsumptionKW} es null
 */
public record EnergyMetricsDto(
        double totalConsumptionKW,
        double estimatedDailyCost,
        Double peakConsumptionKW,
        int totalStations,
        int activeStations,
        double averageLoadPercent,
        String peakConsumptionNote) {

    public static final double PRICE_PER_KWH = 150.0;

    public static final String PEAK_UNAVAILABLE_REASON =
            "Sin sesiones de carga completadas hoy con duración medible: no se puede calcular la potencia pico.";

    public static EnergyMetricsDto from(
            double totalConsumptionKWh,
            Double peakConsumptionKW,
            int totalStations,
            int activeStations,
            int activeSessions) {
        double cost = totalConsumptionKWh * PRICE_PER_KWH;
        double loadPercent = totalStations > 0 ? Math.min(100.0, (activeSessions * 100.0) / totalStations) : 0.0;

        return new EnergyMetricsDto(
                totalConsumptionKWh,
                cost,
                peakConsumptionKW,
                totalStations,
                activeStations,
                loadPercent,
                peakConsumptionKW == null ? PEAK_UNAVAILABLE_REASON : null);
    }
}
