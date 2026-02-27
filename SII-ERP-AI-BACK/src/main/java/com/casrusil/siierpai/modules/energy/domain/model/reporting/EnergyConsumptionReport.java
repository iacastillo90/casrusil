package com.casrusil.siierpai.modules.energy.domain.model.reporting;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for energy consumption reports.
 * Provides detailed breakdown of energy usage by station and connector.
 */
public class EnergyConsumptionReport {

    private final ReportId id;
    private final ReportPeriod period;
    private final LocalDateTime generatedAt;
    private final List<StationConsumption> stationBreakdown;
    private final BigDecimal totalEnergyKwh;
    private final BigDecimal averagePowerKw;
    private final Integer totalSessions;
    private final Duration totalChargingTime;

    private EnergyConsumptionReport(Builder builder) {
        this.id = builder.id;
        this.period = builder.period;
        this.generatedAt = builder.generatedAt;
        this.stationBreakdown = new ArrayList<>(builder.stationBreakdown);
        this.totalEnergyKwh = builder.totalEnergyKwh;
        this.averagePowerKw = builder.averagePowerKw;
        this.totalSessions = builder.totalSessions;
        this.totalChargingTime = builder.totalChargingTime;
    }

    // Getters
    public ReportId getId() {
        return id;
    }

    public ReportPeriod getPeriod() {
        return period;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public List<StationConsumption> getStationBreakdown() {
        return new ArrayList<>(stationBreakdown);
    }

    public BigDecimal getTotalEnergyKwh() {
        return totalEnergyKwh;
    }

    public BigDecimal getAveragePowerKw() {
        return averagePowerKw;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public Duration getTotalChargingTime() {
        return totalChargingTime;
    }

    /**
     * Station-level consumption breakdown.
     */
    public static class StationConsumption {
        private final StationId stationId;
        private final String stationName;
        private final BigDecimal energyKwh;
        private final Integer sessionCount;
        private final Duration chargingTime;
        private final List<ConnectorConsumption> connectorBreakdown;

        public StationConsumption(
                StationId stationId,
                String stationName,
                BigDecimal energyKwh,
                Integer sessionCount,
                Duration chargingTime,
                List<ConnectorConsumption> connectorBreakdown) {
            this.stationId = stationId;
            this.stationName = stationName;
            this.energyKwh = energyKwh;
            this.sessionCount = sessionCount;
            this.chargingTime = chargingTime;
            this.connectorBreakdown = connectorBreakdown != null ? new ArrayList<>(connectorBreakdown)
                    : new ArrayList<>();
        }

        public StationId getStationId() {
            return stationId;
        }

        public String getStationName() {
            return stationName;
        }

        public BigDecimal getEnergyKwh() {
            return energyKwh;
        }

        public Integer getSessionCount() {
            return sessionCount;
        }

        public Duration getChargingTime() {
            return chargingTime;
        }

        public List<ConnectorConsumption> getConnectorBreakdown() {
            return new ArrayList<>(connectorBreakdown);
        }
    }

    /**
     * Connector-level consumption breakdown.
     */
    public static class ConnectorConsumption {
        private final Integer connectorId;
        private final BigDecimal energyKwh;
        private final Integer sessionCount;

        public ConnectorConsumption(Integer connectorId, BigDecimal energyKwh, Integer sessionCount) {
            this.connectorId = connectorId;
            this.energyKwh = energyKwh;
            this.sessionCount = sessionCount;
        }

        public Integer getConnectorId() {
            return connectorId;
        }

        public BigDecimal getEnergyKwh() {
            return energyKwh;
        }

        public Integer getSessionCount() {
            return sessionCount;
        }
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ReportId id;
        private ReportPeriod period;
        private LocalDateTime generatedAt = LocalDateTime.now();
        private List<StationConsumption> stationBreakdown = new ArrayList<>();
        private BigDecimal totalEnergyKwh = BigDecimal.ZERO;
        private BigDecimal averagePowerKw = BigDecimal.ZERO;
        private Integer totalSessions = 0;
        private Duration totalChargingTime = Duration.ZERO;

        public Builder id(ReportId id) {
            this.id = id;
            return this;
        }

        public Builder period(ReportPeriod period) {
            this.period = period;
            return this;
        }

        public Builder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public Builder stationBreakdown(List<StationConsumption> stationBreakdown) {
            this.stationBreakdown = new ArrayList<>(stationBreakdown);
            return this;
        }

        public Builder totalEnergyKwh(BigDecimal totalEnergyKwh) {
            this.totalEnergyKwh = totalEnergyKwh;
            return this;
        }

        public Builder averagePowerKw(BigDecimal averagePowerKw) {
            this.averagePowerKw = averagePowerKw;
            return this;
        }

        public Builder totalSessions(Integer totalSessions) {
            this.totalSessions = totalSessions;
            return this;
        }

        public Builder totalChargingTime(Duration totalChargingTime) {
            this.totalChargingTime = totalChargingTime;
            return this;
        }

        public EnergyConsumptionReport build() {
            if (id == null) {
                id = ReportId.generate();
            }
            if (period == null) {
                throw new IllegalStateException("Report period is required");
            }
            return new EnergyConsumptionReport(this);
        }
    }
}
