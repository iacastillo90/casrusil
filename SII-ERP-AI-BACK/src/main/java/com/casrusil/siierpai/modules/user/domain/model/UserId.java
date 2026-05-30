package com.casrusil.siierpai.modules.user.domain.model;

import java.util.UUID;

/**
 * Value object representing a unique user identifier.
 */
public record UserId(UUID value) {

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("UserId value cannot be null");
        }
        return new UserId(value);
    }

    public static UserId of(String value) {
        return of(UUID.fromString(value));
    }
}
