package com.casrusil.siierpai.modules.user.application.service;

import com.casrusil.siierpai.modules.user.domain.model.*;
import com.casrusil.siierpai.modules.user.domain.port.out.UserRepository;
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

    private final UserRepository userRepository;

    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

        if (userRepository.findByEmail(email.toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.create(organizationId, email, firstName, lastName, passwordHash);
        user = userRepository.save(user);

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

        if (userRepository.findByEmail(email.toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.createInvited(organizationId, email, firstName, lastName);
        user = userRepository.save(user);

        logger.info("User invited: {}", user.getId());
        return user;
    }

    /**
     * Gets user by ID.
     */
    public User getUser(UserId id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    /**
     * Gets user by email.
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    /**
     * Lists all users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Lists users by organization.
     */
    public List<User> getUsersByOrganization(OrganizationId organizationId) {
        return userRepository.findByOrganizationId(organizationId);
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
        return userRepository.save(user);
    }

    /**
     * Verifies user email.
     */
    public User verifyEmail(UserId id) {
        logger.info("Verifying email for user: {}", id);

        User user = getUser(id);
        user.verifyEmail();
        return userRepository.save(user);
    }

    /**
     * Activates invited user.
     */
    public User activateUser(UserId id, String passwordHash) {
        logger.info("Activating user: {}", id);

        User user = getUser(id);
        user.activate(passwordHash);
        return userRepository.save(user);
    }

    /**
     * Suspends user.
     */
    public User suspendUser(UserId id) {
        logger.info("Suspending user: {}", id);

        User user = getUser(id);
        user.suspend();
        return userRepository.save(user);
    }

    /**
     * Assigns role to user.
     */
    public User assignRole(UserId userId, RoleId roleId) {
        logger.info("Assigning role {} to user {}", roleId, userId);

        User user = getUser(userId);
        user.assignRole(roleId);
        return userRepository.save(user);
    }

    /**
     * Removes role from user.
     */
    public User removeRole(UserId userId, RoleId roleId) {
        logger.info("Removing role {} from user {}", roleId, userId);

        User user = getUser(userId);
        user.removeRole(roleId);
        return userRepository.save(user);
    }
}
