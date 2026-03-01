package com.casrusil.siierpai.modules.energy.domain.model.security;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique certificate identifier.
 */
public class CertificateId {
    private final UUID value;

    private CertificateId(UUID value) {
        this.value = Objects.requireNonNull(value, "Certificate ID cannot be null");
    }

    public static CertificateId of(UUID value) {
        return new CertificateId(value);
    }

    public static CertificateId generate() {
        return new CertificateId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CertificateId that = (CertificateId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
