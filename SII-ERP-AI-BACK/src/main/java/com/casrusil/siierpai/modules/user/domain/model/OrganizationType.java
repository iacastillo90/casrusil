package com.casrusil.siierpai.modules.user.domain.model;

/**
 * Enumeration of organization types.
 */
public enum OrganizationType {
    ENTERPRISE("Enterprise", "Large enterprise organization"),
    SMB("Small/Medium Business", "Small to medium business"),
    STARTUP("Startup", "Startup organization"),
    INDIVIDUAL("Individual", "Individual operator"),
    PARTNER("Partner", "Partner organization");

    private final String displayName;
    private final String description;

    OrganizationType(String displayName, String description) {
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
