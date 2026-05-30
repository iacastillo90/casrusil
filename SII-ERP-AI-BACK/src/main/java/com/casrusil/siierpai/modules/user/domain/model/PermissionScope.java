package com.casrusil.siierpai.modules.user.domain.model;

/**
 * Enumeration of permission scopes.
 */
public enum PermissionScope {
    OWN("Own", "User's own resources only"),
    ORGANIZATION("Organization", "All resources within user's organization"),
    ALL("All", "All resources across all organizations");

    private final String displayName;
    private final String description;

    PermissionScope(String displayName, String description) {
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
