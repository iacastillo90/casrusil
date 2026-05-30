package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA: Medición de Energía.
 * Mapea el Value Object EnergyMeasurement del dominio.
 */
@Entity
@Table(name = "energy_measurements")
public class EnergyMeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "energy_kwh", nullable = false, precision = 10, scale = 3)
    private BigDecimal energyKwh;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "meter_value", length = 500)
    private String meterValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChargingSessionEntity session;

    protected EnergyMeasurementEntity() {
        // Constructor vacío requerido por JPA
    }

    public EnergyMeasurementEntity(BigDecimal energyKwh, LocalDateTime measuredAt, String meterValue) {
        this.energyKwh = energyKwh;
        this.measuredAt = measuredAt;
        this.meterValue = meterValue;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(BigDecimal energyKwh) {
        this.energyKwh = energyKwh;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public String getMeterValue() {
        return meterValue;
    }

    public void setMeterValue(String meterValue) {
        this.meterValue = meterValue;
    }

    public ChargingSessionEntity getSession() {
        return session;
    }

    public void setSession(ChargingSessionEntity session) {
        this.session = session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EnergyMeasurementEntity that = (EnergyMeasurementEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
