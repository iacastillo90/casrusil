package com.casrusil.siierpai.modules.energy.domain.model.security;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Certificate aggregate root for OCPP 1.6 Security Extensions.
 * 
 * Represents an X.509 certificate used for secure communication
 * between charging stations and the central system.
 */
public class Certificate {
    private final CertificateId id;
    private final CertificateType type;
    private final String certificateData; // PEM-encoded certificate
    private final String serialNumber;
    private final String issuer;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private CertificateStatus status;
    private final StationId stationId; // Optional - null for root certificates
    private final LocalDateTime createdAt;

    private Certificate(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Certificate ID is required");
        this.type = Objects.requireNonNull(builder.type, "Certificate type is required");
        this.certificateData = Objects.requireNonNull(builder.certificateData, "Certificate data is required");
        this.serialNumber = builder.serialNumber;
        this.issuer = builder.issuer;
        this.validFrom = builder.validFrom;
        this.validTo = builder.validTo;
        this.status = Objects.requireNonNull(builder.status, "Certificate status is required");
        this.stationId = builder.stationId;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
    }

    /**
     * Creates a new certificate.
     */
    public static Certificate create(
            CertificateType type,
            String certificateData,
            String serialNumber,
            String issuer,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            StationId stationId) {

        return builder()
                .id(CertificateId.generate())
                .type(type)
                .certificateData(certificateData)
                .serialNumber(serialNumber)
                .issuer(issuer)
                .validFrom(validFrom)
                .validTo(validTo)
                .status(CertificateStatus.ACCEPTED)
                .stationId(stationId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Checks if the certificate is currently valid.
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == CertificateStatus.ACCEPTED
                && validFrom != null
                && validTo != null
                && !now.isBefore(validFrom)
                && !now.isAfter(validTo);
    }

    /**
     * Checks if the certificate is expired.
     */
    public boolean isExpired() {
        return validTo != null && LocalDateTime.now().isAfter(validTo);
    }

    /**
     * Marks the certificate as rejected.
     */
    public void reject() {
        this.status = CertificateStatus.REJECTED;
    }

    /**
     * Marks the certificate installation as failed.
     */
    public void markFailed() {
        this.status = CertificateStatus.FAILED;
    }

    // Getters
    public CertificateId getId() {
        return id;
    }

    public CertificateType getType() {
        return type;
    }

    public String getCertificateData() {
        return certificateData;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getIssuer() {
        return issuer;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public StationId getStationId() {
        return stationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CertificateId id;
        private CertificateType type;
        private String certificateData;
        private String serialNumber;
        private String issuer;
        private LocalDateTime validFrom;
        private LocalDateTime validTo;
        private CertificateStatus status;
        private StationId stationId;
        private LocalDateTime createdAt;

        public Builder id(CertificateId id) {
            this.id = id;
            return this;
        }

        public Builder type(CertificateType type) {
            this.type = type;
            return this;
        }

        public Builder certificateData(String certificateData) {
            this.certificateData = certificateData;
            return this;
        }

        public Builder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public Builder validFrom(LocalDateTime validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public Builder validTo(LocalDateTime validTo) {
            this.validTo = validTo;
            return this;
        }

        public Builder status(CertificateStatus status) {
            this.status = status;
            return this;
        }

        public Builder stationId(StationId stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Certificate build() {
            return new Certificate(this);
        }
    }
}
