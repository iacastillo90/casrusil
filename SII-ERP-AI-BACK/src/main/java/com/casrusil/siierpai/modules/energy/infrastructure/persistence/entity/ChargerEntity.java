package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.energy.domain.model.ChargerStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad JPA: Cargador.
 * Mapea la entidad de dominio Charger a la base de datos.
 */
@Entity
@Table(name = "chargers")
public class ChargerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocpp_id", nullable = false, unique = true)
    private String ocppId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargerStatus status;

    @Column(nullable = false)
    private Boolean active = false;

    @Column(name = "installation_date", nullable = false)
    private LocalDateTime installationDate;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStationEntity station;

    @OneToMany(mappedBy = "charger", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConnectorEntity> connectors = new ArrayList<>();

    protected ChargerEntity() {
        // Constructor vacío requerido por JPA
    }

    public ChargerEntity(String ocppId, String name, String alias, String manufacturer,
            String model, ChargerStatus status, Boolean active,
            LocalDateTime installationDate) {
        this.ocppId = ocppId;
        this.name = name;
        this.alias = alias;
        this.manufacturer = manufacturer;
        this.model = model;
        this.status = status;
        this.active = active;
        this.installationDate = installationDate;
    }

    // Helper methods para gestionar la relación bidireccional

    public void addConnector(ConnectorEntity connector) {
        connectors.add(connector);
        connector.setCharger(this);
    }

    public void removeConnector(ConnectorEntity connector) {
        connectors.remove(connector);
        connector.setCharger(null);
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOcppId() {
        return ocppId;
    }

    public void setOcppId(String ocppId) {
        this.ocppId = ocppId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public ChargerStatus getStatus() {
        return status;
    }

    public void setStatus(ChargerStatus status) {
        this.status = status;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDateTime installationDate) {
        this.installationDate = installationDate;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public ChargingStationEntity getStation() {
        return station;
    }

    public void setStation(ChargingStationEntity station) {
        this.station = station;
    }

    public List<ConnectorEntity> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<ConnectorEntity> connectors) {
        this.connectors = connectors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChargerEntity that = (ChargerEntity) o;
        return Objects.equals(ocppId, that.ocppId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ocppId);
    }
}
