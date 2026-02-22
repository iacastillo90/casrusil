package com.casrusil.siierpai.modules.energy.domain.model.metadata;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique manufacturer identifier.
 */
public class ManufacturerId {
    private final UUID value;

    private ManufacturerId(UUID value) {
        this.value = Objects.requireNonNull(value, "Manufacturer ID cannot be null");
    }

    public static ManufacturerId of(UUID value) {
        return new ManufacturerId(value);
    }

    public static ManufacturerId generate() {
        return new ManufacturerId(UUID.randomUUID());
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
        ManufacturerId that = (ManufacturerId) o;
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
