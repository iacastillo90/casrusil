package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.DteSendResult;

/**
 * Response body for a DTE submission attempt.
 */
public record DteSendResultDto(boolean success, String message) {

    public static DteSendResultDto from(DteSendResult result) {
        return new DteSendResultDto(result.success(), result.message());
    }
}
