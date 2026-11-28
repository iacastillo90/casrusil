package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase.InvitationPreview;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Aceptación pública de invitaciones (R11).
 *
 * <p>El invitado todavía no tiene cuenta, así que estos endpoints no exigen sesión
 * (viven bajo {@code /api/v1/auth/**}). El token es aleatorio de 256 bits, de un solo
 * uso y vence a los 7 días: usado o vencido responde 410, inexistente 404. El token
 * va en el body y no en la URL para que no quede en logs de acceso.</p>
 */
@RestController
@RequestMapping("/api/v1/auth/invitations")
public class InvitationController {

    private final InviteUserUseCase inviteUserUseCase;

    public InvitationController(InviteUserUseCase inviteUserUseCase) {
        this.inviteUserUseCase = inviteUserUseCase;
    }

    @PostMapping("/validate")
    public ResponseEntity<InvitationPreview> validate(@Valid @RequestBody ValidateInvitationRequest request) {
        return ResponseEntity.ok(inviteUserUseCase.previewInvitation(request.token()));
    }

    @PostMapping("/accept")
    public ResponseEntity<AcceptedInvitationResponse> accept(@Valid @RequestBody AcceptInvitationRequest request) {
        User user = inviteUserUseCase.acceptInvitation(request.token(), request.fullName(), request.password());
        return ResponseEntity.ok(new AcceptedInvitationResponse(user.getId().getValue(), user.getEmail(),
                user.getRole().name()));
    }

    public record ValidateInvitationRequest(@NotBlank String token) {
    }

    public record AcceptInvitationRequest(
            @NotBlank String token,
            @NotBlank String fullName,
            @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password) {
    }

    /** Respuesta mínima: sin hash de contraseña ni ids de empresa. */
    public record AcceptedInvitationResponse(UUID userId, String email, String role) {
    }
}
