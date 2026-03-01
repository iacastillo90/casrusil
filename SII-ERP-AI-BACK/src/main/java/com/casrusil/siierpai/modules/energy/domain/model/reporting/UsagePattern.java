package com.casrusil.siierpai.modules.energy.domain.model.reporting;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * Value object representing usage patterns analysis.
 */
public record UsagePattern(
        List<Integer> peakHours,
        List<Integer> offPeakHours,
        List<DayOfWeek> busiestDays,
        Duration averageSessionDuration,
        Double utilizationRate,
        PeakDemandInfo peakDemand) {
    public UsagePattern {
        if (peakHours == null) {
            throw new IllegalArgumentException("Peak hours cannot be null");
        }
        if (offPeakHours == null) {
            throw new IllegalArgumentException("Off-peak hours cannot be null");
        }
        if (utilizationRate != null && (utilizationRate < 0 || utilizationRate > 100)) {
            throw new IllegalArgumentException("Utilization rate must be between 0 and 100");
        }
    }

    /**
     * Information about peak demand periods.
     */
    public record PeakDemandInfo(
            LocalTime peakTime,
            Integer peakDemandKw,
            DayOfWeek peakDay) {
    }
}
