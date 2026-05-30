package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

/**
 * OCPP 1.6 Security Extension: CertificateSigned.conf
 */
public record CertificateSignedResponse(
        String status // Accepted, Rejected
) {
}
