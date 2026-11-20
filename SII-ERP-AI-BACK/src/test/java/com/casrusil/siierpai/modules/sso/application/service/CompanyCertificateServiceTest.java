package com.casrusil.siierpai.modules.sso.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.CompanyCertificate;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyCertificateRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.crypto.SecretCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompanyCertificateServiceTest {

    private CompanyRepository companyRepository;
    private CompanyCertificateRepository certificateRepository;
    private Pkcs12Handler pkcs12Handler;
    private SecretCipher cipher;
    private CompanyCertificateService service;
    private CompanyId companyId;
    private Company company;

    @BeforeEach
    void setUp() {
        companyRepository = mock(CompanyRepository.class);
        certificateRepository = mock(CompanyCertificateRepository.class);
        pkcs12Handler = mock(Pkcs12Handler.class);
        cipher = new SecretCipher("test-key");
        service = new CompanyCertificateService(companyRepository, certificateRepository, pkcs12Handler, cipher);
        companyId = CompanyId.random();
        company = mock(Company.class);
        when(company.getRut()).thenReturn("76123456-7");
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(certificateRepository.findByCompanyId(companyId.value())).thenReturn(Optional.empty());
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void shouldRejectExpiredCertificate() {
        when(pkcs12Handler.loadCertificate(any(java.io.InputStream.class), any(String.class)))
                .thenReturn(certificate("76123456-7", Instant.now().minusSeconds(60)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.upload(companyId, new byte[] { 1 }, "pass"));

        assertEquals(true, ex.getMessage().contains("vencido"));
    }

    @Test
    void shouldRejectCertificateFromAnotherRut() {
        when(pkcs12Handler.loadCertificate(any(java.io.InputStream.class), any(String.class)))
                .thenReturn(certificate("11111111-1", Instant.now().plusSeconds(3600)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.upload(companyId, new byte[] { 1 }, "pass"));

        assertEquals(true, ex.getMessage().contains("no coincide"));
    }

    @Test
    void shouldStoreCertificateAndPasswordEncrypted() {
        when(pkcs12Handler.loadCertificate(any(java.io.InputStream.class), any(String.class)))
                .thenReturn(certificate("76123456-7", Instant.now().plusSeconds(3600)));

        var info = service.upload(companyId, new byte[] { 1, 2, 3 }, "secreto");

        assertEquals("76123456-7", info.rut());
        ArgumentCaptor<CompanyCertificate> captor = ArgumentCaptor.forClass(CompanyCertificate.class);
        verify(certificateRepository).save(captor.capture());
        CompanyCertificate stored = captor.getValue();
        assertNotEquals("secreto", stored.getPassword());
        assertEquals("secreto", cipher.decrypt(stored.getPassword()));
    }

    private SiiCertificate certificate(String rut, Instant validUntil) {
        return new SiiCertificate(mock(X509Certificate.class), null, rut, Instant.now().minusSeconds(3600),
                validUntil);
    }
}
