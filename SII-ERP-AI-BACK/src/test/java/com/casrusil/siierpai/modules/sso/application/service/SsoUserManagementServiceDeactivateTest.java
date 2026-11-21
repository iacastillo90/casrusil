package com.casrusil.siierpai.modules.sso.application.service;

import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.modules.sso.domain.port.in.ManageCompanyUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.shared.domain.exception.DomainException;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SsoUserManagementServiceDeactivateTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final SsoUserManagementService service = new SsoUserManagementService(
            userRepository,
            mock(PasswordEncoder.class),
            mock(com.casrusil.siierpai.shared.infrastructure.mail.EmailService.class),
            mock(ManageCompanyUseCase.class));

    @Test
    void deactivatesTargetUser() {
        UserId targetId = UserId.generate();
        User user = new User(
                targetId,
                "target@example.com",
                "Target",
                "hash",
                UserRole.USER,
                CompanyId.random(),
                true,
                Instant.now(),
                Map.of());

        when(userRepository.findById(targetId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        service.deactivateUser(targetId, UserId.generate());

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void doesNotAllowSelfDeactivation() {
        UserId userId = UserId.generate();

        assertThrows(DomainException.class,
                () -> service.deactivateUser(userId, userId));

        verify(userRepository, never()).save(any());
    }
}
