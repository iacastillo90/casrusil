package com.casrusil.siierpai.modules.energy.domain.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidad de Dominio: Cargador (Charger).
 * Representa el hardware físico que contiene uno o más conectores.
 * NO es un Agregado Raíz - pertenece al agregado ChargingStation.
 */
public class Charger {
    
    private final OcppIdentifier ocppId;
    private final String name;
    private final String alias;
    private final String manufacturer;
    private final String model;
    private final LocalDateTime installationDate;
    
    private ChargerStatus status;
    private boolean active;
    private final List<Connector> connectors;
    private LocalDateTime lastHeartbeat;
    
    private Charger(Builder builder) {
        this.ocppId = Objects.requireNonNull(builder.ocppId, "OCPP ID no puede ser nulo");
        this.name = Objects.requireNonNull(builder.name, "Nombre no puede ser nulo");
        this.alias = Objects.requireNonNull(builder.alias, "Alias no puede ser nulo");
        this.manufacturer = Objects.requireNonNull(builder.manufacturer, "Fabricante no puede ser nulo");
        this.model = Objects.requireNonNull(builder.model, "Modelo no puede ser nulo");
        this.installationDate = Objects.requireNonNull(builder.installationDate, "Fecha de instalación no puede ser nula");
        this.status = builder.status != null ? builder.status : ChargerStatus.OFFLINE;
        this.active = builder.active;
        this.connectors = new ArrayList<>(builder.connectors);
        this.lastHeartbeat = null;
        
        validateConnectors();
    }
    
    // ========== COMPORTAMIENTO DE DOMINIO ==========
    
    /**
     * Registra un heartbeat del cargador (señal de vida OCPP).
     */
    public void recordHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
        if (this.status == ChargerStatus.OFFLINE) {
            changeStatus(ChargerStatus.ONLINE);
        }
    }
    
    /**
     * Cambia el estado del cargador.
     */
    public void changeStatus(ChargerStatus newStatus) {
        Objects.requireNonNull(newStatus, "El nuevo estado no puede ser nulo");
        
        if (this.status == newStatus) {
            return;
        }
        
        // Si pasa a OFFLINE, marcar todos los conectores como no disponibles
        if (newStatus == ChargerStatus.OFFLINE || newStatus == ChargerStatus.ERROR) {
            connectors.forEach(c -> {
                if (c.getStatus() != ConnectorStatus.UNAVAILABLE) {
                    c.changeStatus(ConnectorStatus.UNAVAILABLE);
                }
            });
        }
        
        this.status = newStatus;
    }
    
    /**
     * Activa el cargador y todos sus conectores.
     */
    public void activate() {
        if (this.active) {
            throw new IllegalStateException("El cargador ya está activo");
        }
        this.active = true;
        changeStatus(ChargerStatus.ONLINE);
    }
    
    /**
     * Desactiva el cargador y todos sus conectores.
     */
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("El cargador ya está inactivo");
        }
        this.active = false;
        changeStatus(ChargerStatus.DISABLED);
        connectors.forEach(Connector::deactivate);
    }
    
    /**
     * Agrega un conector al cargador.
     */
    public void addConnector(Connector connector) {
        Objects.requireNonNull(connector, "El conector no puede ser nulo");
        
        // Validar que no exista un conector con el mismo ID
        if (connectors.stream().anyMatch(c -> c.getConnectorId().equals(connector.getConnectorId()))) {
            throw new IllegalArgumentException("Ya existe un conector con ID: " + connector.getConnectorId());
        }
        
        connectors.add(connector);
    }
    
    /**
     * Obtiene un conector por su ID.
     */
    public Optional<Connector> getConnectorById(ConnectorId connectorId) {
        return connectors.stream()
                .filter(c -> c.getConnectorId().equals(connectorId))
                .findFirst();
    }
    
    /**
     * Verifica si el cargador está en línea.
     */
    public boolean isOnline() {
        return status.isOnline();
    }
    
    /**
     * Verifica si el cargador tiene al menos un conector disponible.
     */
    public boolean hasAvailableConnector() {
        return connectors.stream().anyMatch(c -> c.getStatus().isAvailableForCharging());
    }
    
    /**
     * Cuenta conectores activamente cargando.
     */
    public long countActiveChargingSessions() {
        return connectors.stream()
                .filter(c -> c.getStatus().isActivelyCharging())
                .count();
    }
    
    /**
     * Verifica si el heartbeat está desactualizado (>5 minutos).
     */
    public boolean isHeartbeatStale() {
        if (lastHeartbeat == null) {
            return true;
        }
        return LocalDateTime.now().minusMinutes(5).isAfter(lastHeartbeat);
    }
    
    /**
     * Reporta un error en el cargador.
     */
    public void reportError() {
        changeStatus(ChargerStatus.ERROR);
    }
    
    /**
     * Pone el cargador en mantenimiento.
     */
    public void enterMaintenance() {
        changeStatus(ChargerStatus.MAINTENANCE);
        connectors.forEach(Connector::deactivate);
    }
    
    private void validateConnectors() {
        if (connectors.isEmpty()) {
            throw new IllegalStateException("Un cargador debe tener al menos un conector");
        }
    }
    
    // ========== GETTERS ==========
    
    public OcppIdentifier getOcppId() {
        return ocppId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public String getModel() {
        return model;
    }
    
    public LocalDateTime getInstallationDate() {
        return installationDate;
    }
    
    public ChargerStatus getStatus() {
        return status;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public List<Connector> getConnectors() {
        return Collections.unmodifiableList(connectors);
    }
    
    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    // ========== BUILDER PATTERN ==========
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private OcppIdentifier ocppId;
        private String name;
        private String alias;
        private String manufacturer;
        private String model;
        private LocalDateTime installationDate;
        private ChargerStatus status;
        private boolean active = false;
        private List<Connector> connectors = new ArrayList<>();
        
        public Builder ocppId(OcppIdentifier ocppId) {
            this.ocppId = ocppId;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }
        
        public Builder manufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder installationDate(LocalDateTime installationDate) {
            this.installationDate = installationDate;
            return this;
        }
        
        public Builder status(ChargerStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder active(boolean active) {
            this.active = active;
            return this;
        }
        
        public Builder connectors(List<Connector> connectors) {
            this.connectors = new ArrayList<>(connectors);
            return this;
        }
        
        public Builder addConnector(Connector connector) {
            this.connectors.add(connector);
            return this;
        }
        
        public Charger build() {
            return new Charger(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Charger charger = (Charger) o;
        return Objects.equals(ocppId, charger.ocppId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(ocppId);
    }
    
    @Override
    public String toString() {
        return String.format("Charger[%s, %s, %s, %d connectors]", ocppId, name, status, connectors.size());
    }
}
