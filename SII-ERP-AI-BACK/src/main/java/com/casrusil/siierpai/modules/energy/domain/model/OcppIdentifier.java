package com.casrusil.siierpai.modules.energy.domain.model;

import java.util.Objects;

/**
 * Value Object: Identificador OCPP único del cargador.
 * Este ID es el que el cargador físico reporta en el protocolo OCPP.
 */
public final class OcppIdentifier {

    private final String value;

    private OcppIdentifier(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El OCPP ID no puede estar vacío");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("El OCPP ID no puede exceder 50 caracteres");
        }
        this.value = value.trim();
    }

    public static OcppIdentifier of(String value) {
        return new OcppIdentifier(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OcppIdentifier that = (OcppIdentifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
