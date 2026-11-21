package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.application.service.SsoUserManagementService;
import com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SsoUserControllerDeleteTest {

    private final SsoUserManagementService userService = mock(SsoUserManagementService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new SsoUserController(userService, mock(InviteUserUseCase.class))).build();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCanDeactivateAnotherUser() throws Exception {
        UserId requester = UserId.generate();
        UUID target = UUID.randomUUID();
        authenticate(requester);

        mockMvc.perform(delete("/api/v1/sso/users/{id}", target))
                .andExpect(status().isNoContent());

        verify(userService).deactivateUser(new UserId(target), requester);
    }

    @Test
    void adminCannotDeactivateItself() throws Exception {
        UserId requester = UserId.generate();
        authenticate(requester);

        mockMvc.perform(delete("/api/v1/sso/users/{id}", requester.getValue()))
                .andExpect(status().isConflict());

        verify(userService, never()).deactivateUser(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void withoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/sso/users/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private static void authenticate(UserId userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of()));
    }
}
