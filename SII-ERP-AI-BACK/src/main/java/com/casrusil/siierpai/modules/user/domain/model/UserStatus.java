package com.casrusil.siierpai.modules.user.domain.model;

/**
 * Enumeration of user status.
 */
public enum UserStatus {
    ACTIVE("Active", "User is active and can access the system"),
    INVITED("Invited", "User has been invited but not yet activated"),
    SUSPENDED("Suspended", "User is temporarily suspended"),
    INACTIVE("Inactive", "User account is inactive");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
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
