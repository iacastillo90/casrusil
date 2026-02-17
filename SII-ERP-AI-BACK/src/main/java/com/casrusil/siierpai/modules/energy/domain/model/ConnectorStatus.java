package com.casrusil.siierpai.modules.energy.domain.model;

/**
 * Enum: Estados posibles de un Conector según OCPP.
 * Basado en el estándar OCPP 1.6/2.0.
 */
public enum ConnectorStatus {
    
    /**
     * Conector disponible para iniciar una sesión de carga.
     */
    AVAILABLE("Disponible"),
    
    /**
     * Conector ocupado, cargando activamente.
     */
    CHARGING("Cargando"),
    
    /**
     * Vehículo conectado pero carga suspendida (por EV o EVSE).
     */
    SUSPENDED_EV("Suspendido por Vehículo"),
    SUSPENDED_EVSE("Suspendido por Estación"),
    
    /**
     * Conector reservado para un usuario específico.
     */
    RESERVED("Reservado"),
    
    /**
     * Conector no disponible (falla o mantenimiento).
     */
    UNAVAILABLE("No Disponible"),
    
    /**
     * Conector con falla reportada.
     */
    FAULTED("Con Falla"),
    
    /**
     * Preparando la carga (autenticación, autorización).
     */
    PREPARING("Preparando"),
    
    /**
     * Finalizando la sesión de carga.
     */
    FINISHING("Finalizando");
    
    private final String displayName;
    
    ConnectorStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isAvailableForCharging() {
        return this == AVAILABLE;
    }
    
    public boolean isActivelyCharging() {
        return this == CHARGING;
    }
    
    public boolean isSuspended() {
        return this == SUSPENDED_EV || this == SUSPENDED_EVSE;
    }
    
    public boolean hasError() {
        return this == FAULTED || this == UNAVAILABLE;
    }
}
