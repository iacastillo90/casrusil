package com.casrusil.siierpai.modules.energy.domain.model.maintenance;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique maintenance record identifier.
 */
public class MaintenanceRecordId {
    private final UUID value;

    private MaintenanceRecordId(UUID value) {
        this.value = Objects.requireNonNull(value, "Maintenance record ID cannot be null");
    }

    public static MaintenanceRecordId of(UUID value) {
        return new MaintenanceRecordId(value);
    }

    public static MaintenanceRecordId generate() {
        return new MaintenanceRecordId(UUID.randomUUID());
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
        MaintenanceRecordId that = (MaintenanceRecordId) o;
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
