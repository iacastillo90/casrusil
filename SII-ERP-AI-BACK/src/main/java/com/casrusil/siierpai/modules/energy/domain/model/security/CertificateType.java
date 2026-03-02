package com.casrusil.siierpai.modules.energy.domain.model.security;

/**
 * Certificate types as defined in OCPP 1.6 Security Extensions.
 */
public enum CertificateType {
    /**
     * V2G Root Certificate for ISO 15118 communication.
     */
    V2G_ROOT_CERTIFICATE("V2GRootCertificate"),

    /**
     * MO (Mobility Operator) Root Certificate.
     */
    MO_ROOT_CERTIFICATE("MORootCertificate"),

    /**
     * CSMS (Charging Station Management System) Root Certificate.
     */
    CSMS_ROOT_CERTIFICATE("CSMSRootCertificate"),

    /**
     * Manufacturer Root Certificate.
     */
    MANUFACTURER_ROOT_CERTIFICATE("ManufacturerRootCertificate"),

    /**
     * V2G Certificate Chain for ISO 15118.
     */
    V2G_CERTIFICATE_CHAIN("V2GCertificateChain"),

    /**
     * Charging Station Certificate.
     */
    CHARGING_STATION_CERTIFICATE("ChargingStationCertificate");

    private final String value;

    CertificateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CertificateType fromValue(String value) {
        for (CertificateType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown certificate type: " + value);
    }
}
