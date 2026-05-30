package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Embeddable JPA: Especificaciones eléctricas.
 * Mapea el Value Object ElectricalSpecification del dominio.
 */
@Embeddable
public class ElectricalSpecEmbeddable {

    private BigDecimal maxVoltage;
    private BigDecimal maxPower;
    private BigDecimal maxCurrent;

    protected ElectricalSpecEmbeddable() {
        // Constructor vacío requerido por JPA
    }

    public ElectricalSpecEmbeddable(BigDecimal maxVoltage, BigDecimal maxPower, BigDecimal maxCurrent) {
        this.maxVoltage = maxVoltage;
        this.maxPower = maxPower;
        this.maxCurrent = maxCurrent;
    }

    public BigDecimal getMaxVoltage() {
        return maxVoltage;
    }

    public void setMaxVoltage(BigDecimal maxVoltage) {
        this.maxVoltage = maxVoltage;
    }

    public BigDecimal getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(BigDecimal maxPower) {
        this.maxPower = maxPower;
    }

    public BigDecimal getMaxCurrent() {
        return maxCurrent;
    }

    public void setMaxCurrent(BigDecimal maxCurrent) {
        this.maxCurrent = maxCurrent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ElectricalSpecEmbeddable that = (ElectricalSpecEmbeddable) o;
        return Objects.equals(maxVoltage, that.maxVoltage) &&
                Objects.equals(maxPower, that.maxPower) &&
                Objects.equals(maxCurrent, that.maxCurrent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxVoltage, maxPower, maxCurrent);
    }
}
