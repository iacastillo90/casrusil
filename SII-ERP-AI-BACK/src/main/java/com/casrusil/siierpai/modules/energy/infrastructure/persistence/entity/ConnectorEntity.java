package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.energy.domain.model.ConnectorStatus;
import com.casrusil.siierpai.modules.energy.domain.model.ConnectorType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA: Conector de Carga.
 * Mapea la entidad de dominio Connector a la base de datos.
 * 
 * IMPORTANTE: Esta es una entidad de infraestructura, NO del dominio.
 */
@Entity
@Table(name = "connectors")
public class ConnectorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "connector_number", nullable = false)
    private Integer connectorNumber;

    @Column(nullable = false)
    private String alias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectorType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectorStatus status;

    @Embedded
    private ElectricalSpecEmbeddable electricalSpec;

    @Column(nullable = false)
    private Boolean active = false;

    @Column(name = "last_status_change")
    private LocalDateTime lastStatusChange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private ChargerEntity charger;

    protected ConnectorEntity() {
        // Constructor vacío requerido por JPA
    }

    public ConnectorEntity(Integer connectorNumber, String alias, ConnectorType type,
            ConnectorStatus status, ElectricalSpecEmbeddable electricalSpec,
            Boolean active, LocalDateTime lastStatusChange) {
        this.connectorNumber = connectorNumber;
        this.alias = alias;
        this.type = type;
        this.status = status;
        this.electricalSpec = electricalSpec;
        this.active = active;
        this.lastStatusChange = lastStatusChange;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getConnectorNumber() {
        return connectorNumber;
    }

    public void setConnectorNumber(Integer connectorNumber) {
        this.connectorNumber = connectorNumber;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ConnectorType getType() {
        return type;
    }

    public void setType(ConnectorType type) {
        this.type = type;
    }

    public ConnectorStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectorStatus status) {
        this.status = status;
    }

    public ElectricalSpecEmbeddable getElectricalSpec() {
        return electricalSpec;
    }

    public void setElectricalSpec(ElectricalSpecEmbeddable electricalSpec) {
        this.electricalSpec = electricalSpec;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(LocalDateTime lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }

    public ChargerEntity getCharger() {
        return charger;
    }

    public void setCharger(ChargerEntity charger) {
        this.charger = charger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConnectorEntity that = (ConnectorEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
