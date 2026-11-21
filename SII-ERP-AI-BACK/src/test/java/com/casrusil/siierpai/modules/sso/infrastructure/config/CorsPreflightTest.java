package com.casrusil.siierpai.modules.sso.infrastructure.config;

import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.sso.infrastructure.security.SecurityFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CorsPreflightTest.CorsProbeController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3008")
class CorsPreflightTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityFilter securityFilter;

    @MockBean
    private CompanyRepository companyRepository;

    @MockBean
    private AccountRepository accountRepository;

    @Test
    void preflightAllowsXCompanyIdHeader() throws Exception {
        mockMvc.perform(options("/api/v1/auth/me")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3008")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                "x-company-id,authorization,content-type,accept,x-requested-with"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3008"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        containsStringIgnoringCase("x-company-id")));
    }

    @RestController
    static class CorsProbeController {
        @GetMapping("/api/v1/auth/me")
        String me() {
            return "ok";
        }
    }
}
