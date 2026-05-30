package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

import java.util.List;

/**
 * OCPP 1.6 Security Extension: GetInstalledCertificateIds.req
 */
public record GetInstalledCertificateIdsRequest(
        List<String> certificateType // Optional list of certificate types to filter
) {
}
