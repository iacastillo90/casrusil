package com.casrusil.siierpai.modules.user.domain.model;

import java.util.UUID;

/**
 * Value object representing a unique organization identifier.
 */
public record OrganizationId(UUID value) {

    public static OrganizationId generate() {
        return new OrganizationId(UUID.randomUUID());
    }

    public static OrganizationId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("OrganizationId value cannot be null");
        }
        return new OrganizationId(value);
    }

    public static OrganizationId of(String value) {
        return of(UUID.fromString(value));
    }
}
