package com.casrusil.siierpai.modules.sso.infrastructure.security;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityFilterMeTest {

    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
    private final SecurityFilter filter = new SecurityFilter(jwtTokenProvider, tokenBlacklistService);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void me_isNotPublicAndSetsAuthenticationFromJwt() throws Exception {
        UserId userId = UserId.generate();
        CompanyId companyId = CompanyId.random();
        Claims claims = mock(Claims.class);
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(jwtTokenProvider.validateToken("token")).thenReturn(true);
        when(jwtTokenProvider.getClaims("token")).thenReturn(claims);
        when(jwtTokenProvider.getCompanyIdFromToken("token")).thenReturn(companyId);
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(userId);
        when(tokenBlacklistService.isBlacklisted("token")).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> chainInvoked.set(true));

        assertTrue(chainInvoked.get());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loginRemainsPublicAndDoesNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> chainInvoked.set(true));

        assertTrue(chainInvoked.get());
        assertEquals(null, SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider, never()).validateToken("token");
    }
}
