package com.casrusil.siierpai.modules.energy.domain.model.monitoring;

/**
 * Enumeration of alert types in the system.
 */
public enum AlertType {
    // Station Alerts
    STATION_OFFLINE("Station Offline", "A charging station has gone offline"),
    STATION_ERROR("Station Error", "A charging station has reported an error"),
    STATION_MAINTENANCE_DUE("Maintenance Due", "Station maintenance is due"),

    // Demand Alerts
    HIGH_DEMAND("High Demand", "System experiencing high demand"),
    PEAK_LOAD("Peak Load", "Peak load threshold exceeded"),
    CAPACITY_WARNING("Capacity Warning", "System approaching capacity limit"),

    // Session Alerts
    SESSION_TIMEOUT("Session Timeout", "Charging session has timed out"),
    SESSION_ERROR("Session Error", "Error occurred during charging session"),
    LONG_SESSION("Long Session", "Session duration exceeds normal threshold"),

    // Revenue Alerts
    LOW_REVENUE("Low Revenue", "Revenue below expected threshold"),
    REVENUE_MILESTONE("Revenue Milestone", "Revenue milestone achieved"),
    PRICING_ANOMALY("Pricing Anomaly", "Unusual pricing pattern detected"),

    // System Alerts
    SYSTEM_ERROR("System Error", "System-level error occurred"),
    DATABASE_ERROR("Database Error", "Database connectivity issue"),
    API_ERROR("API Error", "External API error"),

    // Energy Alerts
    HIGH_ENERGY_CONSUMPTION("High Energy Consumption", "Energy consumption above normal"),
    LOW_EFFICIENCY("Low Efficiency", "Energy efficiency below threshold"),
    POWER_SPIKE("Power Spike", "Sudden power consumption spike detected");

    private final String displayName;
    private final String description;

    AlertType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
