package com.casrusil.siierpai.modules.user.domain.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate root representing a user in the system.
 */
public class User {
    private final UserId id;
    private final OrganizationId organizationId;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus status;
    private boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private final Set<RoleId> roleIds;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "User ID is required");
        this.organizationId = Objects.requireNonNull(builder.organizationId, "Organization ID is required");
        this.email = Objects.requireNonNull(builder.email, "Email is required");
        this.passwordHash = builder.passwordHash;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.phone = builder.phone;
        this.status = Objects.requireNonNull(builder.status, "Status is required");
        this.emailVerified = builder.emailVerified;
        this.lastLoginAt = builder.lastLoginAt;
        this.roleIds = new HashSet<>(builder.roleIds);
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
        this.updatedAt = builder.updatedAt;

        validateEmail();
    }

    /**
     * Creates a new active user.
     */
    public static User create(
            OrganizationId organizationId,
            String email,
            String firstName,
            String lastName,
            String passwordHash) {

        return builder()
                .id(UserId.generate())
                .organizationId(organizationId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordHash)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an invited user (pending activation).
     */
    public static User createInvited(
            OrganizationId organizationId,
            String email,
            String firstName,
            String lastName) {

        return builder()
                .id(UserId.generate())
                .organizationId(organizationId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .status(UserStatus.INVITED)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates user profile.
     */
    public void updateProfile(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates user email.
     */
    public void updateEmail(String email) {
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.emailVerified = false; // Require re-verification
        this.updatedAt = LocalDateTime.now();
        validateEmail();
    }

    /**
     * Updates password.
     */
    public void updatePassword(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifies email.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates user account.
     */
    public void activate(String passwordHash) {
        if (this.status != UserStatus.INVITED) {
            throw new IllegalStateException("Only invited users can be activated");
        }
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash is required");
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Suspends user account.
     */
    public void suspend() {
        if (this.status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("User is already suspended");
        }
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reactivates suspended user.
     */
    public void reactivate() {
        if (this.status != UserStatus.SUSPENDED) {
            throw new IllegalStateException("Only suspended users can be reactivated");
        }
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Records login.
     */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Assigns a role to the user.
     */
    public void assignRole(RoleId roleId) {
        this.roleIds.add(roleId);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes a role from the user.
     */
    public void removeRole(RoleId roleId) {
        this.roleIds.remove(roleId);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if user has a specific role.
     */
    public boolean hasRole(RoleId roleId) {
        return this.roleIds.contains(roleId);
    }

    /**
     * Checks if user is active.
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * Gets full name.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        return String.format("%s %s",
                firstName != null ? firstName : "",
                lastName != null ? lastName : "").trim();
    }

    private void validateEmail() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // Getters
    public UserId getId() {
        return id;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public Set<RoleId> getRoleIds() {
        return new HashSet<>(roleIds);
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
        private UserId id;
        private OrganizationId organizationId;
        private String email;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private String phone;
        private UserStatus status = UserStatus.ACTIVE;
        private boolean emailVerified = false;
        private LocalDateTime lastLoginAt;
        private Set<RoleId> roleIds = new HashSet<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UserId id) {
            this.id = id;
            return this;
        }

        public Builder organizationId(OrganizationId organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public Builder roleIds(Set<RoleId> roleIds) {
            this.roleIds = roleIds;
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

        public User build() {
            return new User(this);
        }
    }
}
