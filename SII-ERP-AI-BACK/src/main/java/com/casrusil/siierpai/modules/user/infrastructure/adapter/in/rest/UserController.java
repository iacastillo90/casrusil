package com.casrusil.siierpai.modules.user.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.user.application.service.UserManagementService;
import com.casrusil.siierpai.modules.user.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for user management.
 */
// P0 multiempresa: apagado por defecto porque el módulo user/roles/organizations (esquema public) no tiene company_id y no lo usa el login.
// Sin el bean, sus rutas responden 404 como cualquier ruta inexistente. Ver ronda P6.
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "app.features.legacy-user-admin", name = "enabled", havingValue = "true", matchIfMissing = false)
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management and lifecycle")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping
    @Operation(summary = "Create user")
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid CreateUserRequest request) {
        logger.info("Creating user: {}", request.email());

        User user = userManagementService.createUser(
                OrganizationId.of(request.organizationId()),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.passwordHash());

        return ResponseEntity.ok(UserDto.from(user));
    }

    @PostMapping("/invite")
    @Operation(summary = "Invite user")
    public ResponseEntity<UserDto> inviteUser(@RequestBody @Valid InviteUserRequest request) {
        logger.info("Inviting user: {}", request.email());

        User user = userManagementService.inviteUser(
                OrganizationId.of(request.organizationId()),
                request.email(),
                request.firstName(),
                request.lastName());

        return ResponseEntity.ok(UserDto.from(user));
    }

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users.stream()
                .map(UserDto::from)
                .toList());
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "List users by organization")
    public ResponseEntity<List<UserDto>> getUsersByOrganization(@PathVariable UUID organizationId) {
        List<User> users = userManagementService.getUsersByOrganization(
                OrganizationId.of(organizationId));
        return ResponseEntity.ok(users.stream()
                .map(UserDto::from)
                .toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        try {
            User user = userManagementService.getUser(UserId.of(id));
            return ResponseEntity.ok(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserDto> updateProfile(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProfileRequest request) {

        try {
            User user = userManagementService.updateUserProfile(
                    UserId.of(id),
                    request.firstName(),
                    request.lastName(),
                    request.phone());
            return ResponseEntity.ok(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/verify-email")
    @Operation(summary = "Verify user email")
    public ResponseEntity<UserDto> verifyEmail(@PathVariable UUID id) {
        try {
            User user = userManagementService.verifyEmail(UserId.of(id));
            return ResponseEntity.ok(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate invited user")
    public ResponseEntity<UserDto> activateUser(
            @PathVariable UUID id,
            @RequestBody @Valid ActivateUserRequest request) {

        try {
            User user = userManagementService.activateUser(
                    UserId.of(id),
                    request.passwordHash());
            return ResponseEntity.ok(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend user")
    public ResponseEntity<UserDto> suspendUser(@PathVariable UUID id) {
        try {
            User user = userManagementService.suspendUser(UserId.of(id));
            return ResponseEntity.ok(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Assign role to user")
    public ResponseEntity<UserDto> assignRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        try {
            User user = userManagementService.assignRole(
                    UserId.of(userId),
                    RoleId.of(roleId));
            return ResponseEntity.ok(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Remove role from user")
    public ResponseEntity<UserDto> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        try {
            User user = userManagementService.removeRole(
                    UserId.of(userId),
                    RoleId.of(roleId));
            return ResponseEntity.ok(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DTOs
    public record UserDto(
            UUID id,
            UUID organizationId,
            String email,
            String firstName,
            String lastName,
            String phone,
            String status,
            boolean emailVerified,
            LocalDateTime lastLoginAt,
            Set<UUID> roleIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        public static UserDto from(User user) {
            return new UserDto(
                    user.getId().value(),
                    user.getOrganizationId().value(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhone(),
                    user.getStatus().name(),
                    user.isEmailVerified(),
                    user.getLastLoginAt(),
                    user.getRoleIds().stream()
                            .map(RoleId::value)
                            .collect(Collectors.toSet()),
                    user.getCreatedAt(),
                    user.getUpdatedAt());
        }
    }

    public record CreateUserRequest(
            @NotNull UUID organizationId,
            @NotBlank @Email String email,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String passwordHash) {
    }

    public record InviteUserRequest(
            @NotNull UUID organizationId,
            @NotBlank @Email String email,
            @NotBlank String firstName,
            @NotBlank String lastName) {
    }

    public record UpdateProfileRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String phone) {
    }

    public record ActivateUserRequest(
            @NotBlank String passwordHash) {
    }
}
