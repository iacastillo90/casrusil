package com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manejador de Certificados Digitales PKCS#12.
 * 
 * <p>
 * Responsable de cargar y gestionar el certificado digital del contribuyente,
 * necesario para la autenticación en el SII y la firma de documentos.
 * 
 * <p>
 * Funcionalidades:
 * <ul>
 * <li>Cargar keystore desde archivo .p12</li>
 * <li>Extraer clave privada y certificado X.509</li>
 * <li>Validar vigencia del certificado</li>
 * </ul>
 * 
 * @since 1.0
 */
@Component
public class Pkcs12Handler {

    private static final Logger log = LoggerFactory.getLogger(Pkcs12Handler.class);

    /** Valor centinela cuando el certificado no contiene un RUT reconocible (p. ej. certificados dummy de test). */
    public static final String UNKNOWN_RUT = "UNKNOWN-RUT";

    /** OID chileno estándar para el RUT en el SubjectAlternativeName (otherName). */
    private static final String CHILEAN_RUT_OID = "1.3.6.1.4.1.8321.1";

    /** Patrón de RUT chileno: 7-8 dígitos, guion y dígito verificador (0-9 o K). */
    private static final Pattern RUT_PATTERN = Pattern.compile("(\\d{7,8}-[0-9kK])");

    public SiiCertificate loadCertificate(String path, String password) {
        try (InputStream is = getInputStream(path)) {
            return loadCertificate(is, password);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PKCS12 certificate from path: " + path, e);
        }
    }

    private InputStream getInputStream(String path) throws java.io.FileNotFoundException {
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new java.io.FileNotFoundException("Classpath resource not found: " + resourcePath);
            }
            return is;
        } else {
            return new FileInputStream(path);
        }
    }

    public SiiCertificate loadCertificate(InputStream is, String password) {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(is, password.toCharArray());

            Enumeration<String> aliases = keystore.aliases();
            String alias = null;
            while (aliases.hasMoreElements()) {
                alias = aliases.nextElement();
                if (keystore.isKeyEntry(alias)) {
                    break;
                }
            }

            if (alias == null) {
                throw new IllegalArgumentException("No key entry found in PKCS12 file");
            }

            PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, password.toCharArray());
            X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);

            // Extract RUT from Subject DN (CN=...) or other extension if needed
            // For now, we assume RUT is passed separately or extracted from CN if standard
            // format
            // Simplified for this implementation
            String rut = extractRutFromCert(cert);

            return new SiiCertificate(
                    cert,
                    privateKey,
                    rut,
                    cert.getNotBefore().toInstant(),
                    cert.getNotAfter().toInstant());

        } catch (Exception e) {
            throw new RuntimeException("Failed to load PKCS12 certificate", e);
        }
    }

    /**
     * Extrae el RUT del titular del certificado. Estrategia (en orden):
     * <ol>
     *   <li>Campo {@code SERIALNUMBER} (OID 2.5.4.5) del Subject DN — ubicación
     *       estándar en las CA chilenas (E-Cert, Acepta, E-Sign).</li>
     *   <li>SubjectAlternativeName con el OID chileno {@value #CHILEAN_RUT_OID}.</li>
     *   <li>Búsqueda por patrón de RUT en el Subject DN completo (fallback).</li>
     * </ol>
     *
     * @return el RUT normalizado (ej. {@code 12345678-9}) o {@link #UNKNOWN_RUT}
     *         si el certificado no contiene un RUT reconocible.
     */
    String extractRutFromCert(X509Certificate cert) {
        // 1. SERIALNUMBER del Subject DN.
        String fromSerial = extractFromSubjectSerialNumber(cert);
        if (fromSerial != null) {
            return normalizeRut(fromSerial);
        }

        // 2. SubjectAlternativeName con el OID chileno del RUT.
        String fromSan = extractFromSubjectAlternativeNames(cert);
        if (fromSan != null) {
            return normalizeRut(fromSan);
        }

        // 3. Fallback: buscar un patrón de RUT en el Subject DN completo.
        String subjectDN = cert.getSubjectX500Principal().getName(X500Principal.RFC2253);
        Matcher matcher = RUT_PATTERN.matcher(subjectDN);
        if (matcher.find()) {
            return normalizeRut(matcher.group(1));
        }

        log.warn("No se pudo extraer el RUT del certificado (Subject: {}). Se usará el valor '{}'.",
                subjectDN, UNKNOWN_RUT);
        return UNKNOWN_RUT;
    }

    private String extractFromSubjectSerialNumber(X509Certificate cert) {
        try {
            String dn = cert.getSubjectX500Principal().getName(X500Principal.RFC2253);
            LdapName ldapName = new LdapName(dn);
            for (Rdn rdn : ldapName.getRdns()) {
                String type = rdn.getType();
                // RFC2253 usa "SERIALNUMBER"; algunos proveedores usan el OID "2.5.4.5".
                if ("SERIALNUMBER".equalsIgnoreCase(type) || "2.5.4.5".equals(type)
                        || "OID.2.5.4.5".equalsIgnoreCase(type)) {
                    String value = String.valueOf(rdn.getValue());
                    if (RUT_PATTERN.matcher(value).find()) {
                        return value;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("No se pudo parsear el Subject DN para extraer SERIALNUMBER: {}", e.getMessage());
        }
        return null;
    }

    private String extractFromSubjectAlternativeNames(X509Certificate cert) {
        try {
            Collection<List<?>> sans = cert.getSubjectAlternativeNames();
            if (sans == null) {
                return null;
            }
            for (List<?> san : sans) {
                if (san.size() < 2) {
                    continue;
                }
                Object value = san.get(1);
                String text = (value instanceof byte[] bytes)
                        ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
                        : String.valueOf(value);
                // El otherName incluye el OID; buscamos el patrón de RUT en su contenido.
                if (text.contains(CHILEAN_RUT_OID) || RUT_PATTERN.matcher(text).find()) {
                    Matcher m = RUT_PATTERN.matcher(text);
                    if (m.find()) {
                        return m.group(1);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("No se pudieron leer los SubjectAlternativeNames: {}", e.getMessage());
        }
        return null;
    }

    /** Normaliza el RUT a mayúsculas y sin espacios (ej. {@code 12.345.678-k} -> {@code 12345678-K}). */
    private String normalizeRut(String rut) {
        String cleaned = rut.trim().replace(".", "").toUpperCase();
        Matcher m = RUT_PATTERN.matcher(cleaned);
        return m.find() ? m.group(1).toUpperCase() : cleaned;
    }
}
