package com.casrusil.siierpai.modules.energy.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Especificaciones eléctricas de un conector.
 * Encapsula voltaje, potencia y corriente máxima.
 * Inmutable con validaciones de dominio.
 */
public final class ElectricalSpecification {

    private final BigDecimal maxVoltage; // Voltios (V)
    private final BigDecimal maxPower; // Kilovatios (kW)
    private final BigDecimal maxCurrent; // Amperios (A)

    private ElectricalSpecification(BigDecimal maxVoltage, BigDecimal maxPower, BigDecimal maxCurrent) {
        validatePositive(maxVoltage, "El voltaje máximo");
        validatePositive(maxPower, "La potencia máxima");
        validatePositive(maxCurrent, "La corriente máxima");

        this.maxVoltage = maxVoltage;
        this.maxPower = maxPower;
        this.maxCurrent = maxCurrent;
    }

    public static ElectricalSpecification of(BigDecimal maxVoltage, BigDecimal maxPower, BigDecimal maxCurrent) {
        return new ElectricalSpecification(maxVoltage, maxPower, maxCurrent);
    }

    /**
     * Verifica si la especificación soporta carga rápida (>50kW).
     */
    public boolean isFastCharging() {
        return maxPower.compareTo(BigDecimal.valueOf(50)) > 0;
    }

    /**
     * Calcula la potencia teórica basada en voltaje y corriente.
     * P = V * I / 1000 (para obtener kW)
     */
    public BigDecimal calculateTheoreticalPower() {
        return maxVoltage.multiply(maxCurrent).divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP);
    }

    private void validatePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " debe ser mayor a 0");
        }
    }

    public BigDecimal getMaxVoltage() {
        return maxVoltage;
    }

    public BigDecimal getMaxPower() {
        return maxPower;
    }

    public BigDecimal getMaxCurrent() {
        return maxCurrent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ElectricalSpecification that = (ElectricalSpecification) o;
        return Objects.equals(maxVoltage, that.maxVoltage) &&
                Objects.equals(maxPower, that.maxPower) &&
                Objects.equals(maxCurrent, that.maxCurrent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxVoltage, maxPower, maxCurrent);
    }

    @Override
    public String toString() {
        return String.format("ElectricalSpec[%sV, %skW, %sA]", maxVoltage, maxPower, maxCurrent);
    }
}
