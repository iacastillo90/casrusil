package com.casrusil.siierpai.modules.energy.domain.model.monitoring;

/**
 * Enumeration of metric types tracked in the dashboard.
 */
public enum MetricType {
    // Session Metrics
    ACTIVE_SESSIONS("Active Sessions", "count"),
    TOTAL_SESSIONS_TODAY("Total Sessions Today", "count"),
    COMPLETED_SESSIONS_TODAY("Completed Sessions Today", "count"),

    // Energy Metrics
    TOTAL_ENERGY_TODAY("Total Energy Today", "kWh"),
    TOTAL_ENERGY_THIS_WEEK("Total Energy This Week", "kWh"),
    TOTAL_ENERGY_THIS_MONTH("Total Energy This Month", "kWh"),
    CURRENT_POWER_CONSUMPTION("Current Power Consumption", "kW"),
    AVERAGE_ENERGY_PER_SESSION("Average Energy Per Session", "kWh"),

    // Revenue Metrics
    TOTAL_REVENUE_TODAY("Total Revenue Today", "CLP"),
    TOTAL_REVENUE_THIS_WEEK("Total Revenue This Week", "CLP"),
    TOTAL_REVENUE_THIS_MONTH("Total Revenue This Month", "CLP"),
    AVERAGE_REVENUE_PER_SESSION("Average Revenue Per Session", "CLP"),

    // Station Metrics
    ACTIVE_STATIONS("Active Stations", "count"),
    OFFLINE_STATIONS("Offline Stations", "count"),
    TOTAL_STATIONS("Total Stations", "count"),
    STATION_UTILIZATION_RATE("Station Utilization Rate", "%"),

    // Performance Metrics
    AVERAGE_SESSION_DURATION("Average Session Duration", "minutes"),
    PEAK_POWER_TODAY("Peak Power Today", "kW"),
    ENERGY_EFFICIENCY("Energy Efficiency", "%");

    private final String displayName;
    private final String unit;

    MetricType(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }
}
