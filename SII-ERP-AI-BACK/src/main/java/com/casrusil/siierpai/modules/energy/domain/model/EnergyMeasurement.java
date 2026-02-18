package com.casrusil.siierpai.modules.energy.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object: Medición de energía en un momento específico.
 * Usado para registrar el consumo de energía durante una sesión de carga.
 */
public final class EnergyMeasurement {
    
    private final BigDecimal energyKwh;
    private final LocalDateTime measuredAt;
    private final String meterValue; // Valor crudo del medidor (OCPP)
    
    private EnergyMeasurement(BigDecimal energyKwh, LocalDateTime measuredAt, String meterValue) {
        if (energyKwh == null || energyKwh.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La energía no puede ser negativa");
        }
        if (measuredAt == null) {
            throw new IllegalArgumentException("La fecha de medición no puede ser nula");
        }
        
        this.energyKwh = energyKwh;
        this.measuredAt = measuredAt;
        this.meterValue = meterValue;
    }
    
    public static EnergyMeasurement of(BigDecimal energyKwh, LocalDateTime measuredAt) {
        return new EnergyMeasurement(energyKwh, measuredAt, null);
    }
    
    public static EnergyMeasurement of(BigDecimal energyKwh, LocalDateTime measuredAt, String meterValue) {
        return new EnergyMeasurement(energyKwh, measuredAt, meterValue);
    }
    
    /**
     * Calcula la diferencia de energía con otra medición.
     */
    public BigDecimal calculateDifference(EnergyMeasurement other) {
        return this.energyKwh.subtract(other.energyKwh).abs();
    }
    
    /**
     * Verifica si esta medición es posterior a otra.
     */
    public boolean isAfter(EnergyMeasurement other) {
        return this.measuredAt.isAfter(other.measuredAt);
    }
    
    public BigDecimal getEnergyKwh() {
        return energyKwh;
    }
    
    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }
    
    public String getMeterValue() {
        return meterValue;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnergyMeasurement that = (EnergyMeasurement) o;
        return Objects.equals(energyKwh, that.energyKwh) &&
               Objects.equals(measuredAt, that.measuredAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(energyKwh, measuredAt);
    }
    
    @Override
    public String toString() {
        return String.format("%.2f kWh @ %s", energyKwh, measuredAt);
    }
}
