package com.casrusil.siierpai.modules.user.domain.model;

import java.util.UUID;

/**
 * Value object representing a unique role identifier.
 */
public record RoleId(UUID value) {

    public static RoleId generate() {
        return new RoleId(UUID.randomUUID());
    }

    public static RoleId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("RoleId value cannot be null");
        }
        return new RoleId(value);
    }

    public static RoleId of(String value) {
        return of(UUID.fromString(value));
    }
}
