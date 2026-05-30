package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

/**
 * OCPP 1.6 Security Extension: DeleteCertificate.conf
 */
public record DeleteCertificateResponse(
        String status // Accepted, Failed, NotFound
) {
}
