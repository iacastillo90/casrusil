package com.casrusil.siierpai.modules.sso.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.modules.sso.domain.exception.CertificateNotFoundException;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.CompanyCertificate;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyCertificateRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.crypto.SecretCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Carga y recupera el certificado digital de la empresa (onboarding).
 *
 * <p>
 * El .p12 y su contraseña se guardan cifrados con {@link SecretCipher}; la
 * contraseña nunca se registra ni se devuelve en respuestas.
 * </p>
 *
 * <p>
 * Validaciones antes de persistir: contraseña correcta (implícita al abrir el
 * PKCS#12), certificado vigente y RUT del titular igual al RUT de la empresa.
 * </p>
 */
@Service
public class CompanyCertificateService {

    private static final Logger log = LoggerFactory.getLogger(CompanyCertificateService.class);

    private final CompanyRepository companyRepository;
    private final CompanyCertificateRepository certificateRepository;
    private final Pkcs12Handler pkcs12Handler;
    private final SecretCipher cipher;

    public CompanyCertificateService(CompanyRepository companyRepository,
            CompanyCertificateRepository certificateRepository,
            Pkcs12Handler pkcs12Handler,
            SecretCipher cipher) {
        this.companyRepository = companyRepository;
        this.certificateRepository = certificateRepository;
        this.pkcs12Handler = pkcs12Handler;
        this.cipher = cipher;
    }

    /**
     * Valida y guarda el certificado de la empresa.
     *
     * @param companyId empresa dueña
     * @param pkcs12    contenido del archivo .p12
     * @param password  contraseña del PKCS#12
     * @return datos públicos del certificado (sin contraseña)
     */
    public CertificateInfo upload(CompanyId companyId, byte[] pkcs12, String password) {
        if (pkcs12 == null || pkcs12.length == 0) {
            throw new IllegalArgumentException("El archivo del certificado está vacío");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña del certificado es obligatoria");
        }

        SiiCertificate certificate = pkcs12Handler.loadCertificate(new ByteArrayInputStream(pkcs12), password);

        if (certificate.validUntil() != null && !certificate.validUntil().isAfter(Instant.now())) {
            throw new IllegalArgumentException("El certificado está vencido (válido hasta "
                    + certificate.validUntil() + ")");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + companyId.value()));

        String certificateRut = certificate.rut();
        if (certificateRut == null || certificateRut.isBlank()
                || certificateRut.toUpperCase(Locale.ROOT).startsWith("UNKNOWN")) {
            throw new IllegalArgumentException(
                    "No se pudo extraer el RUT del certificado; no es posible validar que corresponda a la empresa");
        }
        if (!normalize(certificateRut).equals(normalize(company.getRut()))) {
            throw new IllegalArgumentException("El RUT del certificado (" + certificateRut
                    + ") no coincide con el RUT de la empresa (" + company.getRut() + ")");
        }

        UUID id = certificateRepository.findByCompanyId(companyId.value())
                .map(CompanyCertificate::getId)
                .orElseGet(UUID::randomUUID);
        LocalDate validUntil = certificate.validUntil() != null
                ? certificate.validUntil().atZone(ZoneOffset.UTC).toLocalDate()
                : null;

        certificateRepository.save(new CompanyCertificate(
                id,
                companyId.value(),
                cipher.encryptBytes(pkcs12),
                cipher.encrypt(password),
                validUntil));

        log.info("Certificado cargado para empresa {} (válido hasta {})", companyId.value(), validUntil);
        return new CertificateInfo(certificateRut, validUntil);
    }

    /**
     * Recupera y descifra el certificado de la empresa.
     *
     * @param companyId empresa
     * @return certificado listo para firmar, o vacío si no está cargado
     */
    public Optional<SiiCertificate> load(CompanyId companyId) {
        return certificateRepository.findByCompanyId(companyId.value())
                .map(stored -> {
                    byte[] p12 = cipher.decryptBytes(stored.getCertificateData());
                    String password = cipher.decrypt(stored.getPassword());
                    return pkcs12Handler.loadCertificate(new ByteArrayInputStream(p12), password);
                });
    }

    /**
     * Descarta el certificado si no existe.
     *
     * @param companyId empresa
     * @return certificado vigente
     */
    public SiiCertificate require(CompanyId companyId) {
        return load(companyId)
                .orElseThrow(() -> new CertificateNotFoundException(
                        "No SII certificate found for company " + companyId.value()));
    }

    private static String normalize(String rut) {
        return rut == null ? "" : rut.replace(".", "").trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Datos públicos del certificado cargado.
     *
     * @param rut        RUT del titular
     * @param validUntil fecha de vencimiento
     */
    public record CertificateInfo(String rut, LocalDate validUntil) {
    }
}
