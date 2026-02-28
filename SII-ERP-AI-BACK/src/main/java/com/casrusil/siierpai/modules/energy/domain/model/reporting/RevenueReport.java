package com.casrusil.siierpai.modules.energy.domain.model.reporting;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain model for revenue reports.
 * Provides detailed breakdown of revenue by station, user, and time period.
 */
public class RevenueReport {

    private final ReportId id;
    private final ReportPeriod period;
    private final LocalDateTime generatedAt;
    private final BigDecimal totalRevenue;
    private final BigDecimal totalCost;
    private final BigDecimal netProfit;
    private final BigDecimal profitMargin;
    private final List<StationRevenue> stationBreakdown;
    private final List<UserRevenue> userBreakdown;

    private RevenueReport(Builder builder) {
        this.id = builder.id;
        this.period = builder.period;
        this.generatedAt = builder.generatedAt;
        this.totalRevenue = builder.totalRevenue;
        this.totalCost = builder.totalCost;
        this.netProfit = builder.netProfit;
        this.profitMargin = builder.profitMargin;
        this.stationBreakdown = new ArrayList<>(builder.stationBreakdown);
        this.userBreakdown = new ArrayList<>(builder.userBreakdown);
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

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public BigDecimal getProfitMargin() {
        return profitMargin;
    }

    public List<StationRevenue> getStationBreakdown() {
        return new ArrayList<>(stationBreakdown);
    }

    public List<UserRevenue> getUserBreakdown() {
        return new ArrayList<>(userBreakdown);
    }

    /**
     * Revenue breakdown by station.
     */
    public static class StationRevenue {
        private final StationId stationId;
        private final String stationName;
        private final BigDecimal revenue;
        private final Integer sessionCount;
        private final BigDecimal averageRevenuePerSession;

        public StationRevenue(
                StationId stationId,
                String stationName,
                BigDecimal revenue,
                Integer sessionCount,
                BigDecimal averageRevenuePerSession) {
            this.stationId = stationId;
            this.stationName = stationName;
            this.revenue = revenue;
            this.sessionCount = sessionCount;
            this.averageRevenuePerSession = averageRevenuePerSession;
        }

        public StationId getStationId() {
            return stationId;
        }

        public String getStationName() {
            return stationName;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public Integer getSessionCount() {
            return sessionCount;
        }

        public BigDecimal getAverageRevenuePerSession() {
            return averageRevenuePerSession;
        }
    }

    /**
     * Revenue breakdown by user.
     */
    public static class UserRevenue {
        private final UUID userId;
        private final String userName;
        private final BigDecimal revenue;
        private final Integer sessionCount;

        public UserRevenue(UUID userId, String userName, BigDecimal revenue, Integer sessionCount) {
            this.userId = userId;
            this.userName = userName;
            this.revenue = revenue;
            this.sessionCount = sessionCount;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public BigDecimal getRevenue() {
            return revenue;
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
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private BigDecimal netProfit = BigDecimal.ZERO;
        private BigDecimal profitMargin = BigDecimal.ZERO;
        private List<StationRevenue> stationBreakdown = new ArrayList<>();
        private List<UserRevenue> userBreakdown = new ArrayList<>();

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

        public Builder totalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
            return this;
        }

        public Builder totalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder netProfit(BigDecimal netProfit) {
            this.netProfit = netProfit;
            return this;
        }

        public Builder profitMargin(BigDecimal profitMargin) {
            this.profitMargin = profitMargin;
            return this;
        }

        public Builder stationBreakdown(List<StationRevenue> stationBreakdown) {
            this.stationBreakdown = new ArrayList<>(stationBreakdown);
            return this;
        }

        public Builder userBreakdown(List<UserRevenue> userBreakdown) {
            this.userBreakdown = new ArrayList<>(userBreakdown);
            return this;
        }

        public RevenueReport build() {
            if (id == null) {
                id = ReportId.generate();
            }
            if (period == null) {
                throw new IllegalStateException("Report period is required");
            }
            return new RevenueReport(this);
        }
    }
}
