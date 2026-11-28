package com.casrusil.siierpai.modules.sso.domain.port.in;

import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserInvitation;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.Instant;

public interface InviteUserUseCase {
    UserInvitation inviteUser(String email, CompanyId companyId, String role);

    /**
     * Datos visibles de una invitación vigente (sin token ni ids internos).
     *
     * @throws com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException si el token no existe
     * @throws com.casrusil.siierpai.modules.sso.domain.exception.InvitationNoLongerValidException si fue usada o venció
     */
    InvitationPreview previewInvitation(String token);

    /**
     * Acepta la invitación (un solo uso) y crea el usuario en la empresa que invitó.
     * Lo puede llamar el invitado sin sesión.
     */
    User acceptInvitation(String token, String fullName, String rawPassword);

    record InvitationPreview(String email, String role, String companyName, Instant expiresAt) {
    }
}
