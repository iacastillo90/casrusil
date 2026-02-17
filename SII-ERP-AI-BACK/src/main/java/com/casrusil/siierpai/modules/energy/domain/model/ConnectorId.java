package com.casrusil.siierpai.modules.energy.domain.model;

import java.util.Objects;

/**
 * Value Object: Identificador de Conector dentro de una estación.
 * Representa el número físico del conector (1, 2, 3...).
 */
public final class ConnectorId {

    private final Integer value;

    private ConnectorId(Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("El número de conector debe ser mayor a 0");
        }
        this.value = value;
    }

    public static ConnectorId of(Integer value) {
        return new ConnectorId(value);
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConnectorId that = (ConnectorId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Connector-" + value;
    }
}
