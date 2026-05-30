package com.casrusil.siierpai.modules.user.domain.model;

import java.util.Objects;
import java.util.Set;

/**
 * Value object representing a permission.
 */
public record Permission(
        String resource,
        Set<PermissionAction> actions,
        PermissionScope scope) {
    public Permission {
        Objects.requireNonNull(resource, "Resource cannot be null");
        Objects.requireNonNull(actions, "Actions cannot be null");
        Objects.requireNonNull(scope, "Scope cannot be null");

        if (resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource cannot be empty");
        }
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("At least one action is required");
        }
    }

    /**
     * Creates a permission with all CRUD actions.
     */
    public static Permission fullAccess(String resource, PermissionScope scope) {
        return new Permission(
                resource,
                Set.of(PermissionAction.CREATE, PermissionAction.READ,
                        PermissionAction.UPDATE, PermissionAction.DELETE),
                scope);
    }

    /**
     * Creates a read-only permission.
     */
    public static Permission readOnly(String resource, PermissionScope scope) {
        return new Permission(resource, Set.of(PermissionAction.READ), scope);
    }

    /**
     * Checks if permission allows specific action.
     */
    public boolean allows(PermissionAction action) {
        return actions.contains(action);
    }

    /**
     * Checks if permission covers another permission.
     */
    public boolean covers(Permission other) {
        return this.resource.equals(other.resource) &&
                this.actions.containsAll(other.actions) &&
                this.scope.ordinal() >= other.scope.ordinal();
    }
}
