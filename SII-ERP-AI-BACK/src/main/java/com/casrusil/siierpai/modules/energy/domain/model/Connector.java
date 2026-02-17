package com.casrusil.siierpai.modules.energy.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de Dominio: Conector de Carga.
 * Representa un punto físico de conexión en un cargador.
 * NO es un Agregado Raíz - pertenece al agregado ChargingStation.
 */
public class Connector {
    
    private final ConnectorId connectorId;
    private final String alias;
    private final ConnectorType type;
    private final ElectricalSpecification electricalSpec;
    
    private ConnectorStatus status;
    private boolean active;
    private LocalDateTime lastStatusChange;
    
    // Constructor privado - usar Builder
    private Connector(Builder builder) {
        this.connectorId = Objects.requireNonNull(builder.connectorId, "ConnectorId no puede ser nulo");
        this.alias = Objects.requireNonNull(builder.alias, "Alias no puede ser nulo");
        this.type = Objects.requireNonNull(builder.type, "Tipo de conector no puede ser nulo");
        this.electricalSpec = Objects.requireNonNull(builder.electricalSpec, "Especificación eléctrica no puede ser nula");
        this.status = builder.status != null ? builder.status : ConnectorStatus.AVAILABLE;
        this.active = builder.active;
        this.lastStatusChange = LocalDateTime.now();
    }
    
    // ========== COMPORTAMIENTO DE DOMINIO ==========
    
    /**
     * Activa el conector para uso.
     */
    public void activate() {
        if (this.active) {
            throw new IllegalStateException("El conector ya está activo");
        }
        this.active = true;
        changeStatus(ConnectorStatus.AVAILABLE);
    }
    
    /**
     * Desactiva el conector.
     */
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("El conector ya está inactivo");
        }
        this.active = false;
        changeStatus(ConnectorStatus.UNAVAILABLE);
    }
    
    /**
     * Cambia el estado del conector con validación de transiciones.
     */
    public void changeStatus(ConnectorStatus newStatus) {
        Objects.requireNonNull(newStatus, "El nuevo estado no puede ser nulo");
        
        if (this.status == newStatus) {
            return; // No hay cambio
        }
        
        validateStatusTransition(newStatus);
        this.status = newStatus;
        this.lastStatusChange = LocalDateTime.now();
    }
    
    /**
     * Inicia una sesión de carga en este conector.
     */
    public void startCharging() {
        if (!active) {
            throw new IllegalStateException("No se puede cargar en un conector inactivo");
        }
        if (!status.isAvailableForCharging()) {
            throw new IllegalStateException("El conector no está disponible para carga. Estado actual: " + status);
        }
        changeStatus(ConnectorStatus.PREPARING);
    }
    
    /**
     * Marca el conector como activamente cargando.
     */
    public void markAsCharging() {
        changeStatus(ConnectorStatus.CHARGING);
    }
    
    /**
     * Suspende la carga.
     */
    public void suspendCharging(boolean byVehicle) {
        if (!status.isActivelyCharging()) {
            throw new IllegalStateException("No hay carga activa para suspender");
        }
        changeStatus(byVehicle ? ConnectorStatus.SUSPENDED_EV : ConnectorStatus.SUSPENDED_EVSE);
    }
    
    /**
     * Finaliza la sesión de carga.
     */
    public void finishCharging() {
        if (!status.isActivelyCharging() && !status.isSuspended()) {
            throw new IllegalStateException("No hay sesión de carga activa");
        }
        changeStatus(ConnectorStatus.FINISHING);
    }
    
    /**
     * Libera el conector después de finalizar la carga.
     */
    public void release() {
        changeStatus(ConnectorStatus.AVAILABLE);
    }
    
    /**
     * Reporta una falla en el conector.
     */
    public void reportFault() {
        changeStatus(ConnectorStatus.FAULTED);
    }
    
    /**
     * Verifica si el conector soporta carga rápida.
     */
    public boolean supportsFastCharging() {
        return electricalSpec.isFastCharging();
    }
    
    private void validateStatusTransition(ConnectorStatus newStatus) {
        // Validaciones de transiciones de estado según OCPP
        if (status == ConnectorStatus.FAULTED && newStatus != ConnectorStatus.UNAVAILABLE) {
            // Solo se puede pasar de FAULTED a UNAVAILABLE (para mantenimiento)
            // La recuperación requiere intervención manual
        }
    }
    
    // ========== GETTERS ==========
    
    public ConnectorId getConnectorId() {
        return connectorId;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public ConnectorType getType() {
        return type;
    }
    
    public ElectricalSpecification getElectricalSpec() {
        return electricalSpec;
    }
    
    public ConnectorStatus getStatus() {
        return status;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public LocalDateTime getLastStatusChange() {
        return lastStatusChange;
    }
    
    // ========== BUILDER PATTERN ==========
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ConnectorId connectorId;
        private String alias;
        private ConnectorType type;
        private ElectricalSpecification electricalSpec;
        private ConnectorStatus status;
        private boolean active = false;
        
        public Builder connectorId(ConnectorId connectorId) {
            this.connectorId = connectorId;
            return this;
        }
        
        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }
        
        public Builder type(ConnectorType type) {
            this.type = type;
            return this;
        }
        
        public Builder electricalSpec(ElectricalSpecification electricalSpec) {
            this.electricalSpec = electricalSpec;
            return this;
        }
        
        public Builder status(ConnectorStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder active(boolean active) {
            this.active = active;
            return this;
        }
        
        public Connector build() {
            return new Connector(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connector connector = (Connector) o;
        return Objects.equals(connectorId, connector.connectorId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(connectorId);
    }
    
    @Override
    public String toString() {
        return String.format("Connector[%s, %s, %s, %s]", connectorId, alias, type, status);
    }
}
