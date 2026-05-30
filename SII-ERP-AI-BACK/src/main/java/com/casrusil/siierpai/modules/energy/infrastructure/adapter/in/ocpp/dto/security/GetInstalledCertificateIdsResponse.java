package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

import java.util.List;

/**
 * OCPP 1.6 Security Extension: GetInstalledCertificateIds.conf
 */
public record GetInstalledCertificateIdsResponse(
        String status, // Accepted, NotFound
        List<CertificateHashDataType> certificateHashData) {
    public record CertificateHashDataType(
            String hashAlgorithm,
            String issuerNameHash,
            String issuerKeyHash,
            String serialNumber,
            String certificateType) {
    }
}
