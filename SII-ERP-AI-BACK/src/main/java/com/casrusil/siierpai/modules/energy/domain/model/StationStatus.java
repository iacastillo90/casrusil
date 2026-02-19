package com.casrusil.siierpai.modules.energy.domain.model;

/**
 * Enum: Estados posibles de una Estación de Carga.
 * Representa el ciclo de vida operacional de la estación.
 */
public enum StationStatus {
    
    /**
     * Estación en construcción, no operativa.
     */
    UNDER_CONSTRUCTION("En Construcción"),
    
    /**
     * Estación operativa y disponible para uso.
     */
    OPERATIONAL("Operativa"),
    
    /**
     * Estación temporalmente fuera de servicio por mantenimiento.
     */
    MAINTENANCE("En Mantenimiento"),
    
    /**
     * Estación desactivada permanentemente.
     */
    DECOMMISSIONED("Desmantelada");
    
    private final String displayName;
    
    StationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isOperational() {
        return this == OPERATIONAL;
    }
    
    public boolean canAcceptCharging() {
        return this == OPERATIONAL;
    }
}
