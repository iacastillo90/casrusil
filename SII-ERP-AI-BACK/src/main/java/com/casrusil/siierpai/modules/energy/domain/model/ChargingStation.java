package com.casrusil.siierpai.modules.energy.domain.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AGREGADO RAÍZ: Estación de Carga (Charging Station).
 * 
 * Responsabilidades:
 * - Gestionar el ciclo de vida de la estación
 * - Coordinar múltiples cargadores
 * - Mantener invariantes de negocio
 * - Emitir eventos de dominio
 * 
 * Patrón: Aggregate Root (DDD)
 * Toda modificación a Chargers y Connectors debe pasar por esta raíz.
 */
public class ChargingStation {
    
    private final StationId stationId;
    private final String name;
    private final StationLocation location;
    private final LocalDateTime createdAt;
    private final UUID companyId; // Referencia a la empresa propietaria
    
    private StationStatus status;
    private boolean active;
    private final List<Charger> chargers;
    private LocalDateTime lastMaintenanceDate;
    
    private ChargingStation(Builder builder) {
        this.stationId = Objects.requireNonNull(builder.stationId, "StationId no puede ser nulo");
        this.name = Objects.requireNonNull(builder.name, "Nombre no puede ser nulo");
        this.location = Objects.requireNonNull(builder.location, "Ubicación no puede ser nula");
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Fecha de creación no puede ser nula");
        this.companyId = Objects.requireNonNull(builder.companyId, "CompanyId no puede ser nulo");
        this.status = builder.status != null ? builder.status : StationStatus.UNDER_CONSTRUCTION;
        this.active = builder.active;
        this.chargers = new ArrayList<>(builder.chargers);
        this.lastMaintenanceDate = builder.lastMaintenanceDate;
        
        validateInvariants();
    }
    
    // ========== COMPORTAMIENTO DE DOMINIO ==========
    
    /**
     * Activa la estación y la pone operativa.
     * Regla de negocio: Solo se puede activar si tiene al menos un cargador.
     */
    public void activate() {
        if (chargers.isEmpty()) {
            throw new IllegalStateException("No se puede activar una estación sin cargadores");
        }
        if (this.active) {
            throw new IllegalStateException("La estación ya está activa");
        }
        
        this.active = true;
        changeStatus(StationStatus.OPERATIONAL);
        
        // Activar todos los cargadores
        chargers.forEach(Charger::activate);
    }
    
    /**
     * Desactiva la estación.
     */
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("La estación ya está inactiva");
        }
        
        this.active = false;
        changeStatus(StationStatus.DECOMMISSIONED);
        
        // Desactivar todos los cargadores
        chargers.forEach(Charger::deactivate);
    }
    
    /**
     * Cambia el estado de la estación.
     */
    public void changeStatus(StationStatus newStatus) {
        Objects.requireNonNull(newStatus, "El nuevo estado no puede ser nulo");
        
        if (this.status == newStatus) {
            return;
        }
        
        validateStatusTransition(newStatus);
        this.status = newStatus;
    }
    
    /**
     * Agrega un cargador a la estación.
     * Invariante: No puede haber dos cargadores con el mismo OCPP ID.
     */
    public void addCharger(Charger charger) {
        Objects.requireNonNull(charger, "El cargador no puede ser nulo");
        
        // Validar unicidad del OCPP ID
        if (chargers.stream().anyMatch(c -> c.getOcppId().equals(charger.getOcppId()))) {
            throw new IllegalArgumentException("Ya existe un cargador con OCPP ID: " + charger.getOcppId());
        }
        
        chargers.add(charger);
    }
    
    /**
     * Remueve un cargador de la estación.
     * Regla: No se puede remover si está activamente cargando.
     */
    public void removeCharger(OcppIdentifier ocppId) {
        Charger charger = findChargerByOcppId(ocppId)
                .orElseThrow(() -> new IllegalArgumentException("Cargador no encontrado: " + ocppId));
        
        if (charger.countActiveChargingSessions() > 0) {
            throw new IllegalStateException("No se puede remover un cargador con sesiones activas");
        }
        
        chargers.remove(charger);
    }
    
    /**
     * Busca un cargador por su OCPP ID.
     */
    public Optional<Charger> findChargerByOcppId(OcppIdentifier ocppId) {
        return chargers.stream()
                .filter(c -> c.getOcppId().equals(ocppId))
                .findFirst();
    }
    
    /**
     * Busca un conector específico en toda la estación.
     */
    public Optional<Connector> findConnector(OcppIdentifier chargerOcppId, ConnectorId connectorId) {
        return findChargerByOcppId(chargerOcppId)
                .flatMap(charger -> charger.getConnectorById(connectorId));
    }
    
    /**
     * Pone la estación en mantenimiento.
     */
    public void enterMaintenance() {
        changeStatus(StationStatus.MAINTENANCE);
        chargers.forEach(Charger::enterMaintenance);
        this.lastMaintenanceDate = LocalDateTime.now();
    }
    
    /**
     * Completa el mantenimiento y vuelve a operación.
     */
    public void completeMaintenance() {
        if (this.status != StationStatus.MAINTENANCE) {
            throw new IllegalStateException("La estación no está en mantenimiento");
        }
        changeStatus(StationStatus.OPERATIONAL);
    }
    
    /**
     * Cuenta el total de conectores en la estación.
     */
    public int getTotalConnectors() {
        return chargers.stream()
                .mapToInt(c -> c.getConnectors().size())
                .sum();
    }
    
    /**
     * Cuenta conectores disponibles para carga.
     */
    public long getAvailableConnectorsCount() {
        return chargers.stream()
                .filter(Charger::isOnline)
                .flatMap(c -> c.getConnectors().stream())
                .filter(c -> c.getStatus().isAvailableForCharging())
                .count();
    }
    
    /**
     * Cuenta sesiones de carga activas en toda la estación.
     */
    public long getActiveChargingSessionsCount() {
        return chargers.stream()
                .mapToLong(Charger::countActiveChargingSessions)
                .sum();
    }
    
    /**
     * Verifica si la estación está completamente disponible.
     */
    public boolean isFullyOperational() {
        return active && 
               status.isOperational() && 
               chargers.stream().anyMatch(Charger::isOnline);
    }
    
    /**
     * Calcula la capacidad total de carga (suma de potencias máximas).
     */
    public double getTotalChargingCapacityKw() {
        return chargers.stream()
                .flatMap(c -> c.getConnectors().stream())
                .mapToDouble(c -> c.getElectricalSpec().getMaxPower().doubleValue())
                .sum();
    }
    
    /**
     * Verifica si la estación soporta carga rápida.
     */
    public boolean supportsFastCharging() {
        return chargers.stream()
                .flatMap(c -> c.getConnectors().stream())
                .anyMatch(Connector::supportsFastCharging);
    }
    
    /**
     * Obtiene todos los cargadores con heartbeat desactualizado.
     */
    public List<Charger> getStaleChargers() {
        return chargers.stream()
                .filter(Charger::isHeartbeatStale)
                .toList();
    }
    
    private void validateStatusTransition(StationStatus newStatus) {
        // Validaciones de transiciones de estado
        if (status == StationStatus.DECOMMISSIONED && newStatus != StationStatus.DECOMMISSIONED) {
            throw new IllegalStateException("No se puede reactivar una estación desmantelada");
        }
        
        if (newStatus == StationStatus.OPERATIONAL && !active) {
            throw new IllegalStateException("No se puede poner en operación una estación inactiva");
        }
    }
    
    private void validateInvariants() {
        // Una estación debe tener un nombre no vacío
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la estación no puede estar vacío");
        }
    }
    
    // ========== GETTERS ==========
    
    public StationId getStationId() {
        return stationId;
    }
    
    public String getName() {
        return name;
    }
    
    public StationLocation getLocation() {
        return location;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public UUID getCompanyId() {
        return companyId;
    }
    
    public StationStatus getStatus() {
        return status;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public List<Charger> getChargers() {
        return Collections.unmodifiableList(chargers);
    }
    
    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }
    
    // ========== BUILDER PATTERN ==========
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private StationId stationId;
        private String name;
        private StationLocation location;
        private LocalDateTime createdAt;
        private UUID companyId;
        private StationStatus status;
        private boolean active = false;
        private List<Charger> chargers = new ArrayList<>();
        private LocalDateTime lastMaintenanceDate;
        
        public Builder stationId(StationId stationId) {
            this.stationId = stationId;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder location(StationLocation location) {
            this.location = location;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder companyId(UUID companyId) {
            this.companyId = companyId;
            return this;
        }
        
        public Builder status(StationStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder active(boolean active) {
            this.active = active;
            return this;
        }
        
        public Builder chargers(List<Charger> chargers) {
            this.chargers = new ArrayList<>(chargers);
            return this;
        }
        
        public Builder addCharger(Charger charger) {
            this.chargers.add(charger);
            return this;
        }
        
        public Builder lastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
            this.lastMaintenanceDate = lastMaintenanceDate;
            return this;
        }
        
        public ChargingStation build() {
            return new ChargingStation(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChargingStation that = (ChargingStation) o;
        return Objects.equals(stationId, that.stationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(stationId);
    }
    
    @Override
    public String toString() {
        return String.format("ChargingStation[%s, %s, %s, %d chargers, %d total connectors]", 
                stationId, name, status, chargers.size(), getTotalConnectors());
    }
}
