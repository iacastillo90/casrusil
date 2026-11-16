package com.casrusil.siierpai.modules.integration_sii.domain.model;

/**
 * Outcome of sending one DTE to the SII.
 *
 * @param success whether the SII accepted the upload
 * @param trackId SII tracking id when available, otherwise {@code null}
 * @param message human-readable detail for the caller
 */
public record DteSendResult(boolean success, String trackId, String message) {
}
