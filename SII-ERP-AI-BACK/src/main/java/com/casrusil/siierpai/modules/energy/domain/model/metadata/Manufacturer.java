package com.casrusil.siierpai.modules.energy.domain.model.metadata;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Manufacturer aggregate root for charger manufacturers.
 */
public class Manufacturer {
    private final ManufacturerId id;
    private String name;
    private String website;
    private String supportEmail;
    private String supportPhone;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Manufacturer(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Manufacturer ID is required");
        this.name = Objects.requireNonNull(builder.name, "Name is required");
        this.website = builder.website;
        this.supportEmail = builder.supportEmail;
        this.supportPhone = builder.supportPhone;
        this.active = builder.active;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
        this.updatedAt = builder.updatedAt;

        validateName(this.name);
    }

    /**
     * Creates a new manufacturer.
     */
    public static Manufacturer create(String name, String website, String supportEmail, String supportPhone) {
        return builder()
                .id(ManufacturerId.generate())
                .name(name)
                .website(website)
                .supportEmail(supportEmail)
                .supportPhone(supportPhone)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates manufacturer information.
     */
    public void update(String name, String website, String supportEmail, String supportPhone) {
        if (name != null) {
            validateName(name);
            this.name = name;
        }
        this.website = website;
        this.supportEmail = supportEmail;
        this.supportPhone = supportPhone;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates the manufacturer.
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivates the manufacturer.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Manufacturer name cannot be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Manufacturer name cannot exceed 100 characters");
        }
    }

    // Getters
    public ManufacturerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWebsite() {
        return website;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public String getSupportPhone() {
        return supportPhone;
    }

    public boolean isActive() {
        return active;
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
        private ManufacturerId id;
        private String name;
        private String website;
        private String supportEmail;
        private String supportPhone;
        private boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(ManufacturerId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder supportEmail(String supportEmail) {
            this.supportEmail = supportEmail;
            return this;
        }

        public Builder supportPhone(String supportPhone) {
            this.supportPhone = supportPhone;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
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

        public Manufacturer build() {
            return new Manufacturer(this);
        }
    }
}
