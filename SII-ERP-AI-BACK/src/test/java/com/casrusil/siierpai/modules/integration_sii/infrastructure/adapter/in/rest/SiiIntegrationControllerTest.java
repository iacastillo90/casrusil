package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.AuthenticateSiiUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.DownloadRcvUseCase;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.shared.domain.event.EventPublisher;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Deuda técnica: {@code fetch-rcv} usaba una empresa aleatoria cuando no había
 * contexto de empresa, y el RCV descargado se publicaba para un tenant
 * inexistente. Ahora exige el contexto.
 */
class SiiIntegrationControllerTest {

    private AuthenticateSiiUseCase authenticate;
    private DownloadRcvUseCase download;
    private Pkcs12Handler pkcs12;
    private EventPublisher publisher;
    private SiiIntegrationController controller;

    @BeforeEach
    void setUp() {
        authenticate = mock(AuthenticateSiiUseCase.class);
        download = mock(DownloadRcvUseCase.class);
        pkcs12 = mock(Pkcs12Handler.class);
        publisher = mock(EventPublisher.class);
        controller = new SiiIntegrationController(authenticate, download, pkcs12, publisher);
        ReflectionTestUtils.setField(controller, "certPath", "/tmp/cert.p12");
        ReflectionTestUtils.setField(controller, "certPassword", "x");
    }

    @Test
    void sinContextoDeEmpresaNoDescargaNiPublica() {
        assertThatThrownBy(() -> controller.fetchRcv("76000000-0", "2026-01", true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("company context");

        verifyNoInteractions(pkcs12, authenticate, download, publisher);
    }

    @Test
    void conContextoUsaLaEmpresaDelContexto() {
        CompanyId company = new CompanyId(UUID.randomUUID());
        SiiCertificate cert = mock(SiiCertificate.class);
        SiiToken token = mock(SiiToken.class);
        when(pkcs12.loadCertificate(anyString(), anyString())).thenReturn(cert);
        when(authenticate.authenticate(cert)).thenReturn(token);
        when(download.downloadPurchaseRegister(eq(token), eq(company), anyString(), anyString()))
                .thenReturn(List.of());

        CompanyContext.runInCompanyContext(company,
                () -> controller.fetchRcv("76000000-0", "2026-01", true));

        verify(download).downloadPurchaseRegister(token, company, "76000000-0", "2026-01");
        verify(download, never()).downloadSalesRegister(any(), any(), anyString(), anyString());
        verify(publisher).publish(any());
    }
}
