package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.application.service.AuthService;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.modules.sso.infrastructure.security.JwtTokenProvider;
import com.casrusil.siierpai.modules.sso.infrastructure.security.LoginRateLimiter;
import com.casrusil.siierpai.modules.sso.infrastructure.security.TokenBlacklistService;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthMeControllerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CompanyRepository companyRepository = mock(CompanyRepository.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(
            mock(AuthService.class),
            mock(LoginRateLimiter.class),
            mock(TokenBlacklistService.class),
            mock(JwtTokenProvider.class),
            userRepository,
            companyRepository)).build();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void me_returnsAuthenticatedUserWithoutSensitiveData() throws Exception {
        Company company = Company.create("76123456-7", "Empresa QA SpA", "qa@example.com");
        UserId userId = UserId.generate();
        User user = new User(
                userId,
                "usuario@example.com",
                "Usuario QA",
                "hash-secreto-no-exponer",
                UserRole.ACCOUNTANT,
                company.getId(),
                true,
                Instant.now(),
                Map.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        authenticate(userId, "ROLE_ACCOUNTANT");

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.getValue().toString()))
                .andExpect(jsonPath("$.email").value("usuario@example.com"))
                .andExpect(jsonPath("$.name").value("Usuario QA"))
                .andExpect(jsonPath("$.role").value("ACCOUNTANT"))
                .andExpect(jsonPath("$.companyId").value(company.getId().getValue().toString()))
                .andExpect(jsonPath("$.companies[0].id").value(company.getId().getValue().toString()))
                .andExpect(jsonPath("$.companies[0].name").value("Empresa QA SpA"))
                .andExpect(jsonPath("$.companies[0].rut").value("76123456-7"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void me_returnsUnauthorizedWithoutSession() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_returnsUnauthorizedWhenUserIsInactive() throws Exception {
        Company company = Company.create("76543210-K", "Empresa Inactiva SpA", "inactiva@example.com");
        UserId userId = UserId.generate();
        User user = new User(
                userId,
                "inactivo@example.com",
                "Usuario Inactivo",
                "hash",
                UserRole.USER,
                company.getId(),
                false,
                Instant.now(),
                Map.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        authenticate(userId, "ROLE_USER");

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    private static void authenticate(UserId userId, String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(authority))));
    }
}
