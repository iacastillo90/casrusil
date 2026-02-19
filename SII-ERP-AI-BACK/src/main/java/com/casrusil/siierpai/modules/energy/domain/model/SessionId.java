package com.casrusil.siierpai.modules.energy.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de una Sesión de Carga.
 */
public final class SessionId {

    private final UUID value;

    private SessionId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El ID de sesión no puede ser nulo");
        }
        this.value = value;
    }

    public static SessionId of(UUID value) {
        return new SessionId(value);
    }

    public static SessionId generate() {
        return new SessionId(UUID.randomUUID());
    }

    public static SessionId fromString(String value) {
        try {
            return new SessionId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato inválido de SessionId: " + value, e);
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
        SessionId sessionId = (SessionId) o;
        return Objects.equals(value, sessionId.value);
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
