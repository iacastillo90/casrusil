package com.casrusil.siierpai.modules.energy.domain.model.reporting;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique report identifier.
 */
public class ReportId {
    private final UUID value;

    private ReportId(UUID value) {
        this.value = Objects.requireNonNull(value, "Report ID cannot be null");
    }

    public static ReportId of(UUID value) {
        return new ReportId(value);
    }

    public static ReportId generate() {
        return new ReportId(UUID.randomUUID());
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
        ReportId reportId = (ReportId) o;
        return Objects.equals(value, reportId.value);
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
