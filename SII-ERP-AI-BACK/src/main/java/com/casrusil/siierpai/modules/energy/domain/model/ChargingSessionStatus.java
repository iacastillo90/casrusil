package com.casrusil.siierpai.modules.energy.domain.model;

/**
 * Enum: Estados posibles de una Sesión de Carga.
 */
public enum ChargingSessionStatus {

    /**
     * Sesión iniciada pero aún no cargando.
     */
    INITIATED("Iniciada"),

    /**
     * Carga activa en progreso.
     */
    CHARGING("Cargando"),

    /**
     * Carga suspendida temporalmente.
     */
    SUSPENDED("Suspendida"),

    /**
     * Sesión completada exitosamente.
     */
    COMPLETED("Completada"),

    /**
     * Sesión fallida o abortada.
     */
    FAILED("Fallida");

    private final String displayName;

    ChargingSessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == CHARGING || this == SUSPENDED;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == FAILED;
    }
}
