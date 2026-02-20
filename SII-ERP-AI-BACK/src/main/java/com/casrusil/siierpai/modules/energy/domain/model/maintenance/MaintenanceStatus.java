package com.casrusil.siierpai.modules.energy.domain.model.maintenance;

/**
 * Maintenance status for tracking maintenance progress.
 */
public enum MaintenanceStatus {
    /**
     * Maintenance scheduled but not started.
     */
    SCHEDULED,

    /**
     * Maintenance in progress.
     */
    IN_PROGRESS,

    /**
     * Maintenance completed successfully.
     */
    COMPLETED,

    /**
     * Maintenance cancelled.
     */
    CANCELLED,

    /**
     * Maintenance failed or requires follow-up.
     */
    FAILED
}
