package com.casrusil.siierpai.modules.energy.domain.model.maintenance;

/**
 * Maintenance types for charger maintenance.
 */
public enum MaintenanceType {
    /**
     * Scheduled preventive maintenance.
     */
    PREVENTIVE,

    /**
     * Corrective maintenance (fixing issues).
     */
    CORRECTIVE,

    /**
     * Emergency maintenance (urgent repairs).
     */
    EMERGENCY,

    /**
     * Routine inspection.
     */
    INSPECTION,

    /**
     * Firmware/software update.
     */
    SOFTWARE_UPDATE
}
