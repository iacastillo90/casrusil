package com.casrusil.siierpai.modules.energy.domain.model.reporting;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Energy Report aggregate root.
 * 
 * Contains comprehensive energy consumption and revenue data for a given
 * period.
 */
public class EnergyReport {
    private final ReportId id;
    private final ReportType type;
    private final ReportPeriod period;
    private final BigDecimal totalEnergyConsumed; // kWh
    private final BigDecimal totalRevenue;
    private final BigDecimal totalCost;
    private final int sessionCount;
    private final Duration averageSessionDuration;
    private final List<HourlyUsage> peakUsageHours;
    private final List<StationUsage> topStations;
    private final LocalDateTime generatedAt;

    private EnergyReport(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Report ID is required");
        this.type = Objects.requireNonNull(builder.type, "Report type is required");
        this.period = Objects.requireNonNull(builder.period, "Report period is required");
        this.totalEnergyConsumed = Objects.requireNonNull(builder.totalEnergyConsumed, "Total energy is required");
        this.totalRevenue = Objects.requireNonNull(builder.totalRevenue, "Total revenue is required");
        this.totalCost = Objects.requireNonNull(builder.totalCost, "Total cost is required");
        this.sessionCount = builder.sessionCount;
        this.averageSessionDuration = builder.averageSessionDuration;
        this.peakUsageHours = builder.peakUsageHours != null ? new ArrayList<>(builder.peakUsageHours)
                : new ArrayList<>();
        this.topStations = builder.topStations != null ? new ArrayList<>(builder.topStations) : new ArrayList<>();
        this.generatedAt = Objects.requireNonNull(builder.generatedAt, "Generated timestamp is required");
    }

    /**
     * Calculates profit margin percentage.
     */
    public BigDecimal getProfitMargin() {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profit = totalRevenue.subtract(totalCost);
        return profit.divide(totalRevenue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Calculates net profit.
     */
    public BigDecimal getNetProfit() {
        return totalRevenue.subtract(totalCost);
    }

    /**
     * Calculates average energy per session.
     */
    public BigDecimal getAverageEnergyPerSession() {
        if (sessionCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalEnergyConsumed.divide(new BigDecimal(sessionCount), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates average revenue per session.
     */
    public BigDecimal getAverageRevenuePerSession() {
        if (sessionCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(new BigDecimal(sessionCount), 2, java.math.RoundingMode.HALF_UP);
    }

    // Getters
    public ReportId getId() {
        return id;
    }

    public ReportType getType() {
        return type;
    }

    public ReportPeriod getPeriod() {
        return period;
    }

    public BigDecimal getTotalEnergyConsumed() {
        return totalEnergyConsumed;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public Duration getAverageSessionDuration() {
        return averageSessionDuration;
    }

    public List<HourlyUsage> getPeakUsageHours() {
        return new ArrayList<>(peakUsageHours);
    }

    public List<StationUsage> getTopStations() {
        return new ArrayList<>(topStations);
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ReportId id;
        private ReportType type;
        private ReportPeriod period;
        private BigDecimal totalEnergyConsumed;
        private BigDecimal totalRevenue;
        private BigDecimal totalCost;
        private int sessionCount;
        private Duration averageSessionDuration;
        private List<HourlyUsage> peakUsageHours;
        private List<StationUsage> topStations;
        private LocalDateTime generatedAt;

        public Builder id(ReportId id) {
            this.id = id;
            return this;
        }

        public Builder type(ReportType type) {
            this.type = type;
            return this;
        }

        public Builder period(ReportPeriod period) {
            this.period = period;
            return this;
        }

        public Builder totalEnergyConsumed(BigDecimal totalEnergyConsumed) {
            this.totalEnergyConsumed = totalEnergyConsumed;
            return this;
        }

        public Builder totalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
            return this;
        }

        public Builder totalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder sessionCount(int sessionCount) {
            this.sessionCount = sessionCount;
            return this;
        }

        public Builder averageSessionDuration(Duration averageSessionDuration) {
            this.averageSessionDuration = averageSessionDuration;
            return this;
        }

        public Builder peakUsageHours(List<HourlyUsage> peakUsageHours) {
            this.peakUsageHours = peakUsageHours;
            return this;
        }

        public Builder topStations(List<StationUsage> topStations) {
            this.topStations = topStations;
            return this;
        }

        public Builder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public EnergyReport build() {
            return new EnergyReport(this);
        }
    }

    /**
     * Hourly usage data.
     */
    public record HourlyUsage(
            int hour,
            BigDecimal energyConsumed,
            int sessionCount) {
    }

    /**
     * Station usage data.
     */
    public record StationUsage(
            String stationId,
            String stationName,
            BigDecimal energyConsumed,
            BigDecimal revenue,
            int sessionCount) {
    }
}
