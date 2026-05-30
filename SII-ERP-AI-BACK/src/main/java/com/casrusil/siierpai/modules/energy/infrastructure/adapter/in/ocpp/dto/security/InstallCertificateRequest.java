package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

/**
 * OCPP 1.6 Security Extension: InstallCertificate.req
 */
public record InstallCertificateRequest(
        String certificateType, // V2GRootCertificate, MORootCertificate, CSMSRootCertificate,
                                // ManufacturerRootCertificate
        String certificate // PEM encoded X.509 certificate
) {
}
