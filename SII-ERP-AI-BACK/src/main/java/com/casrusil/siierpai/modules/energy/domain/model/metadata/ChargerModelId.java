package com.casrusil.siierpai.modules.energy.domain.model.metadata;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique charger model identifier.
 */
public class ChargerModelId {
    private final UUID value;

    private ChargerModelId(UUID value) {
        this.value = Objects.requireNonNull(value, "Charger model ID cannot be null");
    }

    public static ChargerModelId of(UUID value) {
        return new ChargerModelId(value);
    }

    public static ChargerModelId generate() {
        return new ChargerModelId(UUID.randomUUID());
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
        ChargerModelId that = (ChargerModelId) o;
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
