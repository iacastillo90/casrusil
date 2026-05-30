package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

/**
 * OCPP 1.6 Security Extension: DeleteCertificate.req
 */
public record DeleteCertificateRequest(
        CertificateHashDataType certificateHashData) {
    public record CertificateHashDataType(
            String hashAlgorithm, // SHA256, SHA384, SHA512
            String issuerNameHash,
            String issuerKeyHash,
            String serialNumber) {
    }
}
