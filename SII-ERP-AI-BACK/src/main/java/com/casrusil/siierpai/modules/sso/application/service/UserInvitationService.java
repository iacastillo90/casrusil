package com.casrusil.siierpai.modules.sso.application.service;

import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.UserInvitation;
import com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.in.ManageCompanyUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserInvitationRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.shared.infrastructure.mail.EmailService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import com.casrusil.siierpai.modules.sso.domain.exception.InvitationNoLongerValidException;
import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.modules.sso.domain.port.in.ManageUserUseCase;
import com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException;

@Service
public class UserInvitationService implements InviteUserUseCase {

    private final UserInvitationRepository invitationRepository;
    private final EmailService emailService;
    private final ManageCompanyUseCase companyService;
    private final UserRepository userRepository;
    private final ManageUserUseCase manageUserUseCase;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public UserInvitationService(UserInvitationRepository invitationRepository, EmailService emailService,
            ManageCompanyUseCase companyService, UserRepository userRepository,
            ManageUserUseCase manageUserUseCase) {
        this.manageUserUseCase = manageUserUseCase;
        this.invitationRepository = invitationRepository;
        this.emailService = emailService;
        this.companyService = companyService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserInvitation inviteUser(String email, CompanyId companyId, String role) {
        // TODO: Check if user already exists or already invited? For MVP we just create
        // new invite.

        UserInvitation invitation = UserInvitation.create(email, companyId, role);
        UserInvitation savedInvitation = invitationRepository.save(invitation);

        Company company = companyService.getCompany(companyId);

        // Fetch Inviter logic
        // We assume the current user is the inviter.
        // Since we are in a Service, we can check SecurityContext or UserContext.
        // Ideally UserContext should populate this, but for now we look up by ID from
        // Security (which SecurityFilter sets).
        // If system call (no user), fallback to "System" or Company Name.
        String inviterName = "Un administrador";

        // Try to get from SecurityContext
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserId userId) {
            userRepository.findById(userId).ifPresent(u -> {
                // We can't access non-final local variable inviterName safely in lambda if we
                // assign.
                // So we need another way or just use Optional mapping.
            });
            // Better:
            inviterName = userRepository.findById(userId)
                    .map(u -> u.getFullName() != null ? u.getFullName() : u.getEmail())
                    .orElse("Un administrador");
        }

        String inviteLink = frontendUrl + "/register?token=" + savedInvitation.getToken();

        emailService.sendInvitationEmail(email, inviteLink, company, inviterName);

        return savedInvitation;
    }

    @Override
    @Transactional(noRollbackFor = InvitationNoLongerValidException.class)
    public InvitationPreview previewInvitation(String token) {
        UserInvitation invitation = usableInvitation(token);
        String companyName = companyService.getCompany(invitation.getTargetCompanyId()).getRazonSocial();
        return new InvitationPreview(invitation.getEmail(), invitation.getRole(), companyName,
                invitation.getExpiresAt());
    }

    /**
     * Acepta la invitación y crea el usuario en una sola transacción: la invitación
     * queda ACCEPTED antes de crear el usuario, así un segundo intento con el mismo
     * token falla (410) y si la creación falla se revierte todo.
     */
    @Override
    @Transactional(noRollbackFor = InvitationNoLongerValidException.class)
    public User acceptInvitation(String token, String fullName, String rawPassword) {
        UserInvitation invitation = usableInvitation(token);
        invitation.accept();
        invitationRepository.save(invitation);

        UserRole role = parseRole(invitation.getRole());
        return manageUserUseCase.createUser(invitation.getEmail(), fullName, rawPassword, role,
                invitation.getTargetCompanyId());
    }

    private UserInvitation usableInvitation(String token) {
        if (token == null || token.isBlank()) {
            throw new ResourceNotFoundException("Invitación no encontrada");
        }
        UserInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación no encontrada"));
        if (invitation.expireIfDue(Instant.now())) {
            invitationRepository.save(invitation);
        }
        if (invitation.getStatus() != UserInvitation.Status.PENDING) {
            throw new InvitationNoLongerValidException(
                    "La invitación ya fue usada o venció. Pide una nueva al administrador.");
        }
        return invitation;
    }

    private static UserRole parseRole(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.startsWith("ROLE_")) {
            value = value.substring("ROLE_".length());
        }
        return UserRole.valueOf(value);
    }
}
