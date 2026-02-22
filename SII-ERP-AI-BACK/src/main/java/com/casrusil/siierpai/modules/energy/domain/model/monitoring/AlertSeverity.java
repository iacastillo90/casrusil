package com.casrusil.siierpai.modules.energy.domain.model.monitoring;

/**
 * Enumeration of alert severity levels.
 */
public enum AlertSeverity {
    INFO("Info", 1),
    WARNING("Warning", 2),
    CRITICAL("Critical", 3);

    private final String displayName;
    private final int level;

    AlertSeverity(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public boolean isMoreSevereThan(AlertSeverity other) {
        return this.level > other.level;
    }
}
