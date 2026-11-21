package com.casrusil.siierpai.shared.infrastructure.notification;

import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationPreferencesControllerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new NotificationController(mock(NotificationService.class), userRepository)).build();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updatesOnlyKnownPreferencesForAuthenticatedUser() throws Exception {
        UserId userId = UserId.generate();
        User user = new User(
                userId,
                "user@example.com",
                "Usuario",
                "hash",
                UserRole.ADMIN,
                CompanyId.random(),
                true,
                Instant.now(),
                new HashMap<>(Map.of("email", "true")));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        authenticate(userId);

        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"false\",\"push\":\"true\",\"marketing\":\"true\",\"unknown\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("false"))
                .andExpect(jsonPath("$.push").value("true"))
                .andExpect(jsonPath("$.marketing").value("true"))
                .andExpect(jsonPath("$.unknown").doesNotExist());

        assertEquals("false", user.getPreferences().get("email"));
        assertEquals("true", user.getPreferences().get("push"));
        assertEquals("true", user.getPreferences().get("marketing"));
        assertEquals(null, user.getPreferences().get("unknown"));
        verify(userRepository).save(user);
    }

    @Test
    void withoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"false\"}"))
                .andExpect(status().isUnauthorized());
    }

    private static void authenticate(UserId userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of()));
    }
}
