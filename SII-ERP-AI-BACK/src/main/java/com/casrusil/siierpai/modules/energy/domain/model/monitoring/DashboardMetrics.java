package com.casrusil.siierpai.modules.energy.domain.model.monitoring;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate root representing real-time dashboard metrics.
 */
public class DashboardMetrics {
    private final MetricId id;
    private final LocalDateTime timestamp;

    // Session metrics
    private int activeSessions;
    private int totalSessionsToday;
    private int completedSessionsToday;

    // Energy metrics
    private BigDecimal totalEnergyToday;
    private BigDecimal currentPowerConsumption;
    private BigDecimal averageEnergyPerSession;

    // Revenue metrics
    private BigDecimal totalRevenueToday;
    private BigDecimal averageRevenuePerSession;

    // Station metrics
    private int activeStations;
    private int offlineStations;
    private int totalStations;
    private BigDecimal stationUtilizationRate;

    // Performance metrics
    private int averageSessionDurationMinutes;
    private BigDecimal peakPowerToday;

    // Additional metadata
    private final Map<String, Object> metadata;

    private DashboardMetrics(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Metric ID is required");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "Timestamp is required");
        this.activeSessions = builder.activeSessions;
        this.totalSessionsToday = builder.totalSessionsToday;
        this.completedSessionsToday = builder.completedSessionsToday;
        this.totalEnergyToday = builder.totalEnergyToday;
        this.currentPowerConsumption = builder.currentPowerConsumption;
        this.averageEnergyPerSession = builder.averageEnergyPerSession;
        this.totalRevenueToday = builder.totalRevenueToday;
        this.averageRevenuePerSession = builder.averageRevenuePerSession;
        this.activeStations = builder.activeStations;
        this.offlineStations = builder.offlineStations;
        this.totalStations = builder.totalStations;
        this.stationUtilizationRate = builder.stationUtilizationRate;
        this.averageSessionDurationMinutes = builder.averageSessionDurationMinutes;
        this.peakPowerToday = builder.peakPowerToday;
        this.metadata = new HashMap<>(builder.metadata);
    }

    /**
     * Creates a new dashboard metrics snapshot.
     */
    public static DashboardMetrics create() {
        return builder()
                .id(MetricId.generate())
                .timestamp(LocalDateTime.now())
                .totalEnergyToday(BigDecimal.ZERO)
                .currentPowerConsumption(BigDecimal.ZERO)
                .averageEnergyPerSession(BigDecimal.ZERO)
                .totalRevenueToday(BigDecimal.ZERO)
                .averageRevenuePerSession(BigDecimal.ZERO)
                .stationUtilizationRate(BigDecimal.ZERO)
                .peakPowerToday(BigDecimal.ZERO)
                .build();
    }

    // Getters
    public MetricId getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public int getTotalSessionsToday() {
        return totalSessionsToday;
    }

    public int getCompletedSessionsToday() {
        return completedSessionsToday;
    }

    public BigDecimal getTotalEnergyToday() {
        return totalEnergyToday;
    }

    public BigDecimal getCurrentPowerConsumption() {
        return currentPowerConsumption;
    }

    public BigDecimal getAverageEnergyPerSession() {
        return averageEnergyPerSession;
    }

    public BigDecimal getTotalRevenueToday() {
        return totalRevenueToday;
    }

    public BigDecimal getAverageRevenuePerSession() {
        return averageRevenuePerSession;
    }

    public int getActiveStations() {
        return activeStations;
    }

    public int getOfflineStations() {
        return offlineStations;
    }

    public int getTotalStations() {
        return totalStations;
    }

    public BigDecimal getStationUtilizationRate() {
        return stationUtilizationRate;
    }

    public int getAverageSessionDurationMinutes() {
        return averageSessionDurationMinutes;
    }

    public BigDecimal getPeakPowerToday() {
        return peakPowerToday;
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MetricId id;
        private LocalDateTime timestamp;
        private int activeSessions;
        private int totalSessionsToday;
        private int completedSessionsToday;
        private BigDecimal totalEnergyToday = BigDecimal.ZERO;
        private BigDecimal currentPowerConsumption = BigDecimal.ZERO;
        private BigDecimal averageEnergyPerSession = BigDecimal.ZERO;
        private BigDecimal totalRevenueToday = BigDecimal.ZERO;
        private BigDecimal averageRevenuePerSession = BigDecimal.ZERO;
        private int activeStations;
        private int offlineStations;
        private int totalStations;
        private BigDecimal stationUtilizationRate = BigDecimal.ZERO;
        private int averageSessionDurationMinutes;
        private BigDecimal peakPowerToday = BigDecimal.ZERO;
        private Map<String, Object> metadata = new HashMap<>();

        public Builder id(MetricId id) {
            this.id = id;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder activeSessions(int activeSessions) {
            this.activeSessions = activeSessions;
            return this;
        }

        public Builder totalSessionsToday(int totalSessionsToday) {
            this.totalSessionsToday = totalSessionsToday;
            return this;
        }

        public Builder completedSessionsToday(int completedSessionsToday) {
            this.completedSessionsToday = completedSessionsToday;
            return this;
        }

        public Builder totalEnergyToday(BigDecimal totalEnergyToday) {
            this.totalEnergyToday = totalEnergyToday;
            return this;
        }

        public Builder currentPowerConsumption(BigDecimal currentPowerConsumption) {
            this.currentPowerConsumption = currentPowerConsumption;
            return this;
        }

        public Builder averageEnergyPerSession(BigDecimal averageEnergyPerSession) {
            this.averageEnergyPerSession = averageEnergyPerSession;
            return this;
        }

        public Builder totalRevenueToday(BigDecimal totalRevenueToday) {
            this.totalRevenueToday = totalRevenueToday;
            return this;
        }

        public Builder averageRevenuePerSession(BigDecimal averageRevenuePerSession) {
            this.averageRevenuePerSession = averageRevenuePerSession;
            return this;
        }

        public Builder activeStations(int activeStations) {
            this.activeStations = activeStations;
            return this;
        }

        public Builder offlineStations(int offlineStations) {
            this.offlineStations = offlineStations;
            return this;
        }

        public Builder totalStations(int totalStations) {
            this.totalStations = totalStations;
            return this;
        }

        public Builder stationUtilizationRate(BigDecimal stationUtilizationRate) {
            this.stationUtilizationRate = stationUtilizationRate;
            return this;
        }

        public Builder averageSessionDurationMinutes(int averageSessionDurationMinutes) {
            this.averageSessionDurationMinutes = averageSessionDurationMinutes;
            return this;
        }

        public Builder peakPowerToday(BigDecimal peakPowerToday) {
            this.peakPowerToday = peakPowerToday;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public DashboardMetrics build() {
            return new DashboardMetrics(this);
        }
    }
}
