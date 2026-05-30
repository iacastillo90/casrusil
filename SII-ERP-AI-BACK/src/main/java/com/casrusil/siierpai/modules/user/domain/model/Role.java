package com.casrusil.siierpai.modules.user.domain.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate root representing a role with permissions.
 */
public class Role {
    private final RoleId id;
    private final OrganizationId organizationId; // null for system roles
    private String name;
    private String description;
    private final boolean systemRole;
    private final Set<Permission> permissions;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Role(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Role ID is required");
        this.organizationId = builder.organizationId;
        this.name = Objects.requireNonNull(builder.name, "Role name is required");
        this.description = builder.description;
        this.systemRole = builder.systemRole;
        this.permissions = new HashSet<>(builder.permissions);
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
        this.updatedAt = builder.updatedAt;

        validateName();
    }

    /**
     * Creates a custom organization role.
     */
    public static Role createCustom(
            OrganizationId organizationId,
            String name,
            String description,
            Set<Permission> permissions) {

        return builder()
                .id(RoleId.generate())
                .organizationId(organizationId)
                .name(name)
                .description(description)
                .systemRole(false)
                .permissions(permissions)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a system role (available to all organizations).
     */
    public static Role createSystem(
            String name,
            String description,
            Set<Permission> permissions) {

        return builder()
                .id(RoleId.generate())
                .name(name)
                .description(description)
                .systemRole(true)
                .permissions(permissions)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates role details.
     */
    public void update(String name, String description) {
        if (this.systemRole) {
            throw new IllegalStateException("System roles cannot be modified");
        }
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = description;
        this.updatedAt = LocalDateTime.now();
        validateName();
    }

    /**
     * Adds a permission to the role.
     */
    public void addPermission(Permission permission) {
        if (this.systemRole) {
            throw new IllegalStateException("System roles cannot be modified");
        }
        this.permissions.add(permission);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes a permission from the role.
     */
    public void removePermission(Permission permission) {
        if (this.systemRole) {
            throw new IllegalStateException("System roles cannot be modified");
        }
        this.permissions.remove(permission);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates all permissions.
     */
    public void updatePermissions(Set<Permission> permissions) {
        if (this.systemRole) {
            throw new IllegalStateException("System roles cannot be modified");
        }
        this.permissions.clear();
        this.permissions.addAll(permissions);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if role has a specific permission.
     */
    public boolean hasPermission(String resource, PermissionAction action) {
        return permissions.stream()
                .anyMatch(p -> p.resource().equals(resource) && p.allows(action));
    }

    /**
     * Checks if role is a system role.
     */
    public boolean isSystemRole() {
        return systemRole;
    }

    private void validateName() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Role name cannot exceed 100 characters");
        }
    }

    // Getters
    public RoleId getId() {
        return id;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Permission> getPermissions() {
        return new HashSet<>(permissions);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RoleId id;
        private OrganizationId organizationId;
        private String name;
        private String description;
        private boolean systemRole = false;
        private Set<Permission> permissions = new HashSet<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(RoleId id) {
            this.id = id;
            return this;
        }

        public Builder organizationId(OrganizationId organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder systemRole(boolean systemRole) {
            this.systemRole = systemRole;
            return this;
        }

        public Builder permissions(Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Role build() {
            return new Role(this);
        }
    }
}
