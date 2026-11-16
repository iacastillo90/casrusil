package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncResult;

/**
 * Response body for an on-demand SII synchronization.
 */
public record SiiSyncResultDto(boolean success, String message) {

    public static SiiSyncResultDto from(SiiSyncResult result) {
        return new SiiSyncResultDto(result.success(), result.message());
    }
}
