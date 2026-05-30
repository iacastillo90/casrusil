package com.casrusil.siierpai.modules.user.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate root representing an organization in the multi-tenant system.
 */
public class Organization {
    private final OrganizationId id;
    private String name;
    private OrganizationType type;
    private OrganizationStatus status;
    private OrganizationId parentId; // For hierarchical organizations
    private Map<String, Object> settings;
    private Map<String, Object> branding;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Organization(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Organization ID is required");
        this.name = Objects.requireNonNull(builder.name, "Organization name is required");
        this.type = Objects.requireNonNull(builder.type, "Organization type is required");
        this.status = Objects.requireNonNull(builder.status, "Organization status is required");
        this.parentId = builder.parentId;
        this.settings = new HashMap<>(builder.settings);
        this.branding = new HashMap<>(builder.branding);
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
        this.updatedAt = builder.updatedAt;

        validateName();
    }

    /**
     * Creates a new organization.
     */
    public static Organization create(String name, OrganizationType type) {
        return builder()
                .id(OrganizationId.generate())
                .name(name)
                .type(type)
                .status(OrganizationStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a child organization.
     */
    public static Organization createChild(String name, OrganizationType type, OrganizationId parentId) {
        return builder()
                .id(OrganizationId.generate())
                .name(name)
                .type(type)
                .status(OrganizationStatus.ACTIVE)
                .parentId(parentId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates organization details.
     */
    public void update(String name, OrganizationType type) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.updatedAt = LocalDateTime.now();
        validateName();
    }

    /**
     * Suspends the organization.
     */
    public void suspend() {
        if (this.status == OrganizationStatus.SUSPENDED) {
            throw new IllegalStateException("Organization is already suspended");
        }
        this.status = OrganizationStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates the organization.
     */
    public void activate() {
        this.status = OrganizationStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivates the organization.
     */
    public void deactivate() {
        this.status = OrganizationStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates organization settings.
     */
    public void updateSettings(Map<String, Object> settings) {
        this.settings = new HashMap<>(settings);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates organization branding.
     */
    public void updateBranding(Map<String, Object> branding) {
        this.branding = new HashMap<>(branding);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if organization is active.
     */
    public boolean isActive() {
        return this.status == OrganizationStatus.ACTIVE;
    }

    /**
     * Checks if organization has a parent.
     */
    public boolean hasParent() {
        return this.parentId != null;
    }

    private void validateName() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization name cannot be empty");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Organization name cannot exceed 200 characters");
        }
    }

    // Getters
    public OrganizationId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OrganizationType getType() {
        return type;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public OrganizationId getParentId() {
        return parentId;
    }

    public Map<String, Object> getSettings() {
        return new HashMap<>(settings);
    }

    public Map<String, Object> getBranding() {
        return new HashMap<>(branding);
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
        private OrganizationId id;
        private String name;
        private OrganizationType type;
        private OrganizationStatus status = OrganizationStatus.ACTIVE;
        private OrganizationId parentId;
        private Map<String, Object> settings = new HashMap<>();
        private Map<String, Object> branding = new HashMap<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(OrganizationId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(OrganizationType type) {
            this.type = type;
            return this;
        }

        public Builder status(OrganizationStatus status) {
            this.status = status;
            return this;
        }

        public Builder parentId(OrganizationId parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder settings(Map<String, Object> settings) {
            this.settings = settings;
            return this;
        }

        public Builder branding(Map<String, Object> branding) {
            this.branding = branding;
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

        public Organization build() {
            return new Organization(this);
        }
    }
}
