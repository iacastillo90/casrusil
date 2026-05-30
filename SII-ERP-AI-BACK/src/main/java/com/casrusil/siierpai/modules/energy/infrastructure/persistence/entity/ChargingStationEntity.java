package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.energy.domain.model.StationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA: Estación de Carga.
 * Mapea el Aggregate Root ChargingStation del dominio a la base de datos.
 */
@Entity
@Table(name = "charging_stations")
public class ChargingStationEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private LocationEmbeddable location;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StationStatus status;

    @Column(nullable = false)
    private Boolean active = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChargerEntity> chargers = new ArrayList<>();

    protected ChargingStationEntity() {
        // Constructor vacío requerido por JPA
    }

    public ChargingStationEntity(UUID id, String name, LocationEmbeddable location,
            UUID companyId, StationStatus status, Boolean active,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.companyId = companyId;
        this.status = status;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Helper methods para gestionar la relación bidireccional

    public void addCharger(ChargerEntity charger) {
        chargers.add(charger);
        charger.setStation(this);
    }

    public void removeCharger(ChargerEntity charger) {
        chargers.remove(charger);
        charger.setStation(null);
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationEmbeddable getLocation() {
        return location;
    }

    public void setLocation(LocationEmbeddable location) {
        this.location = location;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public List<ChargerEntity> getChargers() {
        return chargers;
    }

    public void setChargers(List<ChargerEntity> chargers) {
        this.chargers = chargers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChargingStationEntity that = (ChargingStationEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
