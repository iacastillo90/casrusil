package com.casrusil.siierpai.modules.energy.domain.model;

/**
 * Enum: Estados posibles de un Cargador (Charger).
 * Representa el estado operacional del hardware físico.
 */
public enum ChargerStatus {
    
    /**
     * Cargador activo y conectado al sistema.
     */
    ONLINE("En Línea"),
    
    /**
     * Cargador desconectado o sin comunicación.
     */
    OFFLINE("Fuera de Línea"),
    
    /**
     * Cargador en mantenimiento programado.
     */
    MAINTENANCE("En Mantenimiento"),
    
    /**
     * Cargador con error crítico.
     */
    ERROR("Error"),
    
    /**
     * Cargador desactivado administrativamente.
     */
    DISABLED("Deshabilitado");
    
    private final String displayName;
    
    ChargerStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isOnline() {
        return this == ONLINE;
    }
    
    public boolean canOperate() {
        return this == ONLINE;
    }
    
    public boolean requiresAttention() {
        return this == ERROR || this == OFFLINE;
    }
}
