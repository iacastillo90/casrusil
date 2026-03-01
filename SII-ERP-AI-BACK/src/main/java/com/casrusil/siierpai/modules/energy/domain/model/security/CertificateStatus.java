package com.casrusil.siierpai.modules.energy.domain.model.security;

/**
 * Certificate status as defined in OCPP 1.6 Security Extensions.
 */
public enum CertificateStatus {
    /**
     * Certificate was accepted and installed.
     */
    ACCEPTED("Accepted"),

    /**
     * Certificate was rejected.
     */
    REJECTED("Rejected"),

    /**
     * Certificate installation failed.
     */
    FAILED("Failed");

    private final String value;

    CertificateStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CertificateStatus fromValue(String value) {
        for (CertificateStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown certificate status: " + value);
    }
}
