package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

/**
 * OCPP 1.6 Security Extension: CertificateSigned.req
 */
public record CertificateSignedRequest(
        String certificateChain // PEM encoded X.509 certificate chain
) {
}
