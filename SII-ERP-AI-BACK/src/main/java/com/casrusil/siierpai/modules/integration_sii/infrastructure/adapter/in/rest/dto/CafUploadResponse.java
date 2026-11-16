package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;

/**
 * Response body for a CAF upload.
 *
 * @param folioRange folio range authorized by the uploaded CAF
 */
public record CafUploadResponse(FolioRange folioRange) {

    public static CafUploadResponse from(Caf caf) {
        return new CafUploadResponse(new FolioRange(caf.rangoDesde(), caf.rangoHasta()));
    }

    /**
     * Authorized folio range.
     */
    public record FolioRange(Long start, Long end) {
    }
}
