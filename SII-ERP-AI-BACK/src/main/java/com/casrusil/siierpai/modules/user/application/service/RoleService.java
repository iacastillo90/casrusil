package com.casrusil.siierpai.modules.user.application.service;

import com.casrusil.siierpai.modules.user.domain.model.*;
import com.casrusil.siierpai.modules.user.domain.port.out.RoleRepository;
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

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        // Initialize system roles if empty
        if (roleRepository.findSystemRoles().isEmpty()) {
            initializeSystemRoles();
        }
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
        role = roleRepository.save(role);

        logger.info("Role created: {}", role.getId());
        return role;
    }

    /**
     * Gets role by ID.
     */
    public Role getRole(RoleId id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + id));
    }

    /**
     * Lists all roles.
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Lists system roles.
     */
    public List<Role> getSystemRoles() {
        return roleRepository.findSystemRoles();
    }

    /**
     * Lists roles by organization.
     */
    public List<Role> getRolesByOrganization(OrganizationId organizationId) {
        return roleRepository.findByOrganizationId(organizationId);
    }

    /**
     * Updates role.
     */
    public Role updateRole(RoleId id, String name, String description) {
        logger.info("Updating role: {}", id);

        Role role = getRole(id);
        role.update(name, description);
        return roleRepository.save(role);
    }

    /**
     * Updates role permissions.
     */
    public Role updatePermissions(RoleId id, Set<Permission> permissions) {
        logger.info("Updating permissions for role: {}", id);

        Role role = getRole(id);
        role.updatePermissions(permissions);
        return roleRepository.save(role);
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
        roleRepository.save(admin);

        // Manager role
        Role manager = Role.createSystem(
                "MANAGER",
                "Organization Manager",
                Set.of(
                        Permission.fullAccess("organizations", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("users", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("stations", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("sessions", PermissionScope.ORGANIZATION)));
        roleRepository.save(manager);

        // Operator role
        Role operator = Role.createSystem(
                "OPERATOR",
                "Station Operator",
                Set.of(
                        Permission.readOnly("organizations", PermissionScope.ORGANIZATION),
                        Permission.readOnly("users", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("stations", PermissionScope.ORGANIZATION),
                        Permission.fullAccess("sessions", PermissionScope.ORGANIZATION)));
        roleRepository.save(operator);

        // Viewer role
        Role viewer = Role.createSystem(
                "VIEWER",
                "Read-only Viewer",
                Set.of(
                        Permission.readOnly("*", PermissionScope.ORGANIZATION)));
        roleRepository.save(viewer);

        logger.info("Initialized system roles");
    }
}
