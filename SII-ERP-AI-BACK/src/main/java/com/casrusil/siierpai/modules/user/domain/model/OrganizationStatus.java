package com.casrusil.siierpai.modules.user.domain.model;

/**
 * Enumeration of organization status.
 */
public enum OrganizationStatus {
    ACTIVE("Active", "Organization is active and operational"),
    SUSPENDED("Suspended", "Organization is temporarily suspended"),
    INACTIVE("Inactive", "Organization is inactive"),
    PENDING("Pending", "Organization setup is pending");

    private final String displayName;
    private final String description;

    OrganizationStatus(String displayName, String description) {
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
