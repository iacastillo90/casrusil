package com.casrusil.siierpai.modules.user.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.user.application.service.RoleService;
import com.casrusil.siierpai.modules.user.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for role and permission management.
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "RBAC role and permission management")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @Operation(summary = "Create custom role")
    public ResponseEntity<RoleDto> createRole(@RequestBody @Valid CreateRoleRequest request) {
        logger.info("Creating role: {}", request.name());

        Set<Permission> permissions = request.permissions().stream()
                .map(p -> new Permission(p.resource(), p.actions(), p.scope()))
                .collect(Collectors.toSet());

        Role role = roleService.createRole(
                OrganizationId.of(request.organizationId()),
                request.name(),
                request.description(),
                permissions);

        return ResponseEntity.ok(RoleDto.from(role));
    }

    @GetMapping
    @Operation(summary = "List all roles")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles.stream()
                .map(RoleDto::from)
                .toList());
    }

    @GetMapping("/system")
    @Operation(summary = "List system roles")
    public ResponseEntity<List<RoleDto>> getSystemRoles() {
        List<Role> roles = roleService.getSystemRoles();
        return ResponseEntity.ok(roles.stream()
                .map(RoleDto::from)
                .toList());
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "List roles by organization")
    public ResponseEntity<List<RoleDto>> getRolesByOrganization(@PathVariable UUID organizationId) {
        List<Role> roles = roleService.getRolesByOrganization(OrganizationId.of(organizationId));
        return ResponseEntity.ok(roles.stream()
                .map(RoleDto::from)
                .toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleDto> getRole(@PathVariable UUID id) {
        try {
            Role role = roleService.getRole(RoleId.of(id));
            return ResponseEntity.ok(RoleDto.from(role));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role")
    public ResponseEntity<RoleDto> updateRole(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateRoleRequest request) {

        try {
            Role role = roleService.updateRole(
                    RoleId.of(id),
                    request.name(),
                    request.description());
            return ResponseEntity.ok(RoleDto.from(role));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "Update role permissions")
    public ResponseEntity<RoleDto> updatePermissions(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePermissionsRequest request) {

        try {
            Set<Permission> permissions = request.permissions().stream()
                    .map(p -> new Permission(p.resource(), p.actions(), p.scope()))
                    .collect(Collectors.toSet());

            Role role = roleService.updatePermissions(RoleId.of(id), permissions);
            return ResponseEntity.ok(RoleDto.from(role));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DTOs
    public record RoleDto(
            UUID id,
            UUID organizationId,
            String name,
            String description,
            boolean systemRole,
            Set<PermissionDto> permissions,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        public static RoleDto from(Role role) {
            return new RoleDto(
                    role.getId().value(),
                    role.getOrganizationId() != null ? role.getOrganizationId().value() : null,
                    role.getName(),
                    role.getDescription(),
                    role.isSystemRole(),
                    role.getPermissions().stream()
                            .map(PermissionDto::from)
                            .collect(Collectors.toSet()),
                    role.getCreatedAt(),
                    role.getUpdatedAt());
        }
    }

    public record PermissionDto(
            String resource,
            Set<PermissionAction> actions,
            PermissionScope scope) {
        public static PermissionDto from(Permission permission) {
            return new PermissionDto(
                    permission.resource(),
                    permission.actions(),
                    permission.scope());
        }
    }

    public record CreateRoleRequest(
            @NotNull UUID organizationId,
            @NotBlank String name,
            String description,
            @NotNull Set<PermissionDto> permissions) {
    }

    public record UpdateRoleRequest(
            @NotBlank String name,
            String description) {
    }

    public record UpdatePermissionsRequest(
            @NotNull Set<PermissionDto> permissions) {
    }
}
