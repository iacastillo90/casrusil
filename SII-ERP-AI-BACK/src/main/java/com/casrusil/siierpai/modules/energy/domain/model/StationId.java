package com.casrusil.siierpai.modules.energy.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de una Estación de Carga.
 * Inmutable y con validación de negocio.
 */
public final class StationId {

    private final UUID value;

    private StationId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El ID de la estación no puede ser nulo");
        }
        this.value = value;
    }

    public static StationId of(UUID value) {
        return new StationId(value);
    }

    public static StationId generate() {
        return new StationId(UUID.randomUUID());
    }

    public static StationId fromString(String value) {
        try {
            return new StationId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato inválido de StationId: " + value, e);
        }
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
        StationId stationId = (StationId) o;
        return Objects.equals(value, stationId.value);
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
