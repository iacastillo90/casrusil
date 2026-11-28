package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserInvitation;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserInvitationRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.test.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * R11: el invitado (sin sesión ni rol ADMIN) valida y acepta su invitación con un
 * token de un solo uso y con expiración. También cubre R10: el login del nuevo
 * usuario entrega el JWT solo en la cookie httpOnly, no en el body.
 */
@AutoConfigureMockMvc
@Transactional
class InvitationFlowIntegrationTest extends BaseIntegrationTest {

    private static final String PASSWORD = "ClaveSegura123";

    @Autowired private MockMvc mockMvc;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private UserInvitationRepository invitationRepository;
    @Autowired private UserRepository userRepository;

    private CompanyId company;
    private String email;
    /** IP propia por test: el RateLimitFilter limita /api/v1/auth/** a 10 req/min por IP. */
    private String clientIp;

    @BeforeEach
    void setUp() {
        String stamp = UUID.randomUUID().toString().substring(0, 8);
        company = companyRepository.save(Company.create("77000000-" + stamp.charAt(0), "Invita " + stamp,
                "inv-" + stamp + "@example.com")).getId();
        email = "invitado-" + stamp + "@example.com";
        clientIp = "10." + (int) (Math.random() * 250) + "." + (int) (Math.random() * 250) + "."
                + (int) (Math.random() * 250);
    }

    @Test
    void flujoCompleto_validarAceptarYLoguearSinSerAdmin() throws Exception {
        UserInvitation invitation = invitationRepository.save(UserInvitation.create(email, company, "ACCOUNTANT"));
        assertTrue(invitation.getToken().length() >= 43, "El token debe tener al menos 256 bits");

        mockMvc.perform(post("/api/v1/auth/invitations/validate").contentType(MediaType.APPLICATION_JSON).header("X-Forwarded-For", clientIp)
                        .content(json("token", invitation.getToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("ACCOUNTANT"))
                .andExpect(jsonPath("$.token").doesNotExist());

        mockMvc.perform(post("/api/v1/auth/invitations/accept").contentType(MediaType.APPLICATION_JSON).header("X-Forwarded-For", clientIp)
                        .content(acceptBody(invitation.getToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        User created = userRepository.findByEmail(email).orElseThrow();
        assertEquals(company, created.getCompanyId());
        assertEquals(UserRole.ACCOUNTANT, created.getRole());
        assertEquals(UserInvitation.Status.ACCEPTED,
                invitationRepository.findByToken(invitation.getToken()).orElseThrow().getStatus());

        // R10: el login del invitado devuelve la cookie httpOnly y no expone el JWT en el body.
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).header("X-Forwarded-For", clientIp)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.companyId").value(company.value().toString()));
    }

    @Test
    void tokenReutilizado_responde410YNoCreaOtroUsuario() throws Exception {
        UserInvitation invitation = invitationRepository.save(UserInvitation.create(email, company, "USER"));

        mockMvc.perform(post("/api/v1/auth/invitations/accept").contentType(MediaType.APPLICATION_JSON).header("X-Forwarded-For", clientIp)
                .content(acceptBody(invitation.getToken()))).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/invitations/accept").contentType(MediaType.APPLICATION_JSON).header("X-Forwarded-For", clientIp)
                        .content(acceptBody(invitation.getToken())))
                .andExpect(status().isGone());
        mockMvc.perform(post("/api/v1/auth/invitations/validate").contentType(MediaType.APPLICATION_JSON).header("X-Forwarded-For", clientIp)
                        .content(json("token", invitation.getToken())))
                .andExpect(status().isGone());
    }

    @Test
    void tokenVencido_responde410YQuedaMarcadoComoVencido() throws Exception {
        UserInvitation expired = invitationRepository.save(new UserInvitation(UUID.randomUUID(), email, company,
                "USER", "vencido-" + UUID.randomUUID(), Instant.now().minusSeconds(60),
                UserInvitation.Status.PENDING));

        mockMvc.perform(post("/api/v1/auth/invitations/accept").contentType(MediaType.APPLICATION_JSON).header("X-Forwarded-For", clientIp)
                        .content(acceptBody(expired.getToken())))
                .andExpect(status().isGone());

        assertTrue(userRepository.findByEmail(email).isEmpty(), "No debe crearse usuario con invitación vencida");
        assertEquals(UserInvitation.Status.EXPIRED,
                invitationRepository.findByToken(expired.getToken()).orElseThrow().getStatus());
    }

    @Test
    void tokenInexistente_responde404() throws Exception {
        mockMvc.perform(post("/api/v1/auth/invitations/validate").contentType(MediaType.APPLICATION_JSON).header("X-Forwarded-For", clientIp)
                        .content(json("token", "no-existe")))
                .andExpect(status().isNotFound());
    }

    @Test
    void endpointsAntiguosSoloAdmin_yaNoExisten() throws Exception {
        UserInvitation invitation = invitationRepository.save(UserInvitation.create(email, company, "USER"));
        mockMvc.perform(get("/api/v1/sso/users/invite/validate/{t}", invitation.getToken())
                        .header("X-Forwarded-For", clientIp))
                .andExpect(status().is4xxClientError());
        assertEquals(UserInvitation.Status.PENDING,
                invitationRepository.findByToken(invitation.getToken()).orElseThrow().getStatus());
    }

    private static String json(String key, String value) {
        return "{\"" + key + "\":\"" + value + "\"}";
    }

    private static String acceptBody(String token) {
        return "{\"token\":\"" + token + "\",\"fullName\":\"Persona Invitada\",\"password\":\"" + PASSWORD + "\"}";
    }
}
