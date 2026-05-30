package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

/**
 * OCPP 1.6 Security Extension: InstallCertificate.conf
 */
public record InstallCertificateResponse(
        String status // Accepted, Rejected, Failed
) {
}
