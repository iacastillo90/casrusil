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
 * Application service for role and permission management.
 */
@Service
@Transactional
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    // In-memory storage (TODO: replace with repository)
    private final Map<RoleId, Role> roles = new ConcurrentHashMap<>();

    public RoleService() {
        // Initialize system roles
        initializeSystemRoles();
    }

    /**
     * Creates a custom role.
     */
    public Role createRole(
            OrganizationId organizationId,
            String name,
            String description,
            Set<Permission> permissions) {

        logger.info("Creating role: {} for organization: {}", name, organizationId);

        Role role = Role.createCustom(organizationId, name, description, permissions);
        roles.put(role.getId(), role);

        logger.info("Role created: {}", role.getId());
        return role;
    }

    /**
     * Gets role by ID.
     */
    public Role getRole(RoleId id) {
        Role role = roles.get(id);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + id);
        }
        return role;
    }

    /**
     * Lists all roles.
     */
    public List<Role> getAllRoles() {
        return new ArrayList<>(roles.values());
    }

    /**
     * Lists system roles.
     */
    public List<Role> getSystemRoles() {
        return roles.values().stream()
                .filter(Role::isSystemRole)
                .collect(Collectors.toList());
    }

    /**
     * Lists roles by organization.
     */
    public List<Role> getRolesByOrganization(OrganizationId organizationId) {
        return roles.values().stream()
                .filter(role -> !role.isSystemRole() &&
                        organizationId.equals(role.getOrganizationId()))
                .collect(Collectors.toList());
    }

    /**
     * Updates role.
     */
    public Role updateRole(RoleId id, String name, String description) {
        logger.info("Updating role: {}", id);

        Role role = getRole(id);
        role.update(name, description);

        return role;
    }

    /**
     * Updates role permissions.
     */
    public Role updatePermissions(RoleId id, Set<Permission> permissions) {
        logger.info("Updating permissions for role: {}", id);

        Role role = getRole(id);
        role.updatePermissions(permissions);

        return role;
    }

    /**
     * Initializes default system roles.
     */
    private void initializeSystemRoles() {
        // Admin role - full access
        Role admin = Role.createSystem(
                "ADMIN",
                "System Administrator with full access",
                Set.of(
                        Permission.fullAccess("*", PermissionScope.ALL)));
        roles.put(admin.getId(), admin);

        // Manager role
        Role manager = Role.createSystem(
                "MANAGER",
                "Organization Manager",
                Set.of(
                        Permission.fullAccess("organizations", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("users", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("stations", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("sessions", PermissionScope.ORGANIZATION)));
        roles.put(manager.getId(), manager);

        // Operator role
        Role operator = Role.createSystem(
                "OPERATOR",
                "Station Operator",
                Set.of(
                        Permission.readOnly("organizations", PermissionScope.ORGANIZATION),
                        Permission.readOnly("users", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("stations", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("sessions", PermissionScope.ORGANIZATION)));
        roles.put(operator.getId(), operator);

        // Viewer role
        Role viewer = Role.createSystem(
                "VIEWER",
                "Read-only Viewer",
                Set.of(
                        Permission.readOnly("*", PermissionScope.ORGANIZATION)));
        roles.put(viewer.getId(), viewer);

        logger.info("Initialized {} system roles", roles.size());
    }
}
