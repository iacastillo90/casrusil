package com.casrusil.siierpai.modules.sso.infrastructure.web;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.sso.application.service.SsoUserManagementService;
import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para gestión de usuarios.
 * 
 * <p>
 * Permite crear nuevos usuarios y gestionar roles dentro de la empresa actual.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/users}: Crear nuevo usuario.</li>
 * <li>{@code PUT /api/v1/users/{id}/role}: Actualizar rol de usuario.</li>
 * </ul>
 * 
 * @see UserManagementService
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/sso/users")
@Tag(name = "SSO Users", description = "SSO user management operations")
@PreAuthorize("hasRole('ADMIN')")
public class SsoUserController {

    private final SsoUserManagementService userService;
    private final com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase inviteUserUseCase;

    public SsoUserController(SsoUserManagementService userService,
            com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase inviteUserUseCase) {
        this.userService = userService;
        this.inviteUserUseCase = inviteUserUseCase;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.status(403).build();
        }
        User user = userService.createUser(request.email(), request.fullName(), request.password(), request.role(),
                companyId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        User user = userService.updateUser(new UserId(id), null, request.role());
        return ResponseEntity.ok(user);
    }

    /**
     * Desactiva (soft delete) un usuario de la empresa. Solo ADMIN y nunca el
     * propio usuario autenticado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        UserId requesterId = currentUserId();
        if (requesterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (requesterId.getValue().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        userService.deactivateUser(new UserId(id), requesterId);
        return ResponseEntity.noContent().build();
    }

    private UserId currentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof UserId userId)) {
            return null;
        }
        return userId;
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteUser(@Valid @RequestBody InviteUserRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.status(403).build();
        }
        inviteUserUseCase.inviteUser(request.email(), companyId, request.role().name());
        return ResponseEntity.ok().build();
    }

    // La validación y aceptación de invitaciones son públicas (el invitado no tiene
    // sesión): ver InvitationController en /api/v1/auth/invitations (R11).

    public record CreateUserRequest(String email, String fullName, String password, UserRole role) {
    }

    public record UpdateRoleRequest(UserRole role) {
    }

    public record InviteUserRequest(String email, UserRole role) {
    }
}
