package com.casrusil.siierpai.modules.user.application.service;

import com.casrusil.siierpai.modules.user.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Application service for user management.
 */
@Service
@Transactional
public class UserManagementService {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);

    // In-memory storage (TODO: replace with repository)
    private final Map<UserId, User> users = new ConcurrentHashMap<>();
    private final Map<String, UserId> emailIndex = new ConcurrentHashMap<>();

    /**
     * Creates a new user.
     */
    public User createUser(
            OrganizationId organizationId,
            String email,
            String firstName,
            String lastName,
            String passwordHash) {

        logger.info("Creating user: {}", email);

        if (emailIndex.containsKey(email.toLowerCase())) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.create(organizationId, email, firstName, lastName, passwordHash);
        users.put(user.getId(), user);
        emailIndex.put(email.toLowerCase(), user.getId());

        logger.info("User created: {}", user.getId());
        return user;
    }

    /**
     * Invites a new user.
     */
    public User inviteUser(
            OrganizationId organizationId,
            String email,
            String firstName,
            String lastName) {

        logger.info("Inviting user: {}", email);

        if (emailIndex.containsKey(email.toLowerCase())) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.createInvited(organizationId, email, firstName, lastName);
        users.put(user.getId(), user);
        emailIndex.put(email.toLowerCase(), user.getId());

        logger.info("User invited: {}", user.getId());
        return user;
    }

    /**
     * Gets user by ID.
     */
    public User getUser(UserId id) {
        User user = users.get(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        return user;
    }

    /**
     * Gets user by email.
     */
    public Optional<User> getUserByEmail(String email) {
        UserId userId = emailIndex.get(email.toLowerCase());
        return userId != null ? Optional.of(users.get(userId)) : Optional.empty();
    }

    /**
     * Lists all users.
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    /**
     * Lists users by organization.
     */
    public List<User> getUsersByOrganization(OrganizationId organizationId) {
        return users.values().stream()
                .filter(user -> user.getOrganizationId().equals(organizationId))
                .collect(Collectors.toList());
    }

    /**
     * Updates user profile.
     */
    public User updateUserProfile(
            UserId id,
            String firstName,
            String lastName,
            String phone) {

        logger.info("Updating user profile: {}", id);

        User user = getUser(id);
        user.updateProfile(firstName, lastName, phone);

        return user;
    }

    /**
     * Verifies user email.
     */
    public User verifyEmail(UserId id) {
        logger.info("Verifying email for user: {}", id);

        User user = getUser(id);
        user.verifyEmail();

        return user;
    }

    /**
     * Activates invited user.
     */
    public User activateUser(UserId id, String passwordHash) {
        logger.info("Activating user: {}", id);

        User user = getUser(id);
        user.activate(passwordHash);

        return user;
    }

    /**
     * Suspends user.
     */
    public User suspendUser(UserId id) {
        logger.info("Suspending user: {}", id);

        User user = getUser(id);
        user.suspend();

        return user;
    }

    /**
     * Assigns role to user.
     */
    public User assignRole(UserId userId, RoleId roleId) {
        logger.info("Assigning role {} to user {}", roleId, userId);

        User user = getUser(userId);
        user.assignRole(roleId);

        return user;
    }

    /**
     * Removes role from user.
     */
    public User removeRole(UserId userId, RoleId roleId) {
        logger.info("Removing role {} from user {}", roleId, userId);

        User user = getUser(userId);
        user.removeRole(roleId);

        return user;
    }
}
