package com.casrusil.siierpai.modules.integration_sii.domain.model;

/**
 * Outcome of an on-demand SII synchronization attempt.
 *
 * <p>Business/external failures are reported as {@code success=false} rather
 * than as thrown exceptions, so the REST layer can return HTTP 200.</p>
 */
public record SiiSyncResult(boolean success, String message) {
}
