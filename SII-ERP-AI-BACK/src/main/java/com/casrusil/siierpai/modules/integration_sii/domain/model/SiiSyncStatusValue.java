package com.casrusil.siierpai.modules.integration_sii.domain.model;

/**
 * Health state of an individual SII integration module.
 *
 * <p>Only these three values are ever emitted to clients.</p>
 */
public enum SiiSyncStatusValue {
    SYNCED,
    PENDING,
    ERROR
}
