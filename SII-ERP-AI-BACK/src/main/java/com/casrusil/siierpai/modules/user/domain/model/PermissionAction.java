package com.casrusil.siierpai.modules.user.domain.model;

/**
 * Enumeration of permission actions.
 */
public enum PermissionAction {
    CREATE("Create", "Create new resources"),
    READ("Read", "View/read resources"),
    UPDATE("Update", "Modify existing resources"),
    DELETE("Delete", "Delete resources"),
    EXECUTE("Execute", "Execute operations");

    private final String displayName;
    private final String description;

    PermissionAction(String displayName, String description) {
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
