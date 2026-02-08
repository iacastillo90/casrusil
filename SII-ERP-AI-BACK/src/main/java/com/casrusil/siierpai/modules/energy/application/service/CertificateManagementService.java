package com.casrusil.siierpai.modules.energy.application.service;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.security.Certificate;
import com.casrusil.siierpai.modules.energy.domain.model.security.CertificateId;
import com.casrusil.siierpai.modules.energy.domain.model.security.CertificateStatus;
import com.casrusil.siierpai.modules.energy.domain.model.security.CertificateType;
import com.casrusil.siierpai.modules.energy.domain.port.out.CertificateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

/**
 * Service for managing X.509 certificates in OCPP 1.6 Security Extensions.
 * 
 * Handles certificate installation, deletion, and validation for secure
 * communication between charging stations and the central system.
 */
@Service
@Transactional
public class CertificateManagementService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateManagementService.class);

    private final CertificateRepository certificateRepository;

    public CertificateManagementService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    /**
     * Installs a certificate for a charging station.
     * 
     * @param stationId       Station to install certificate for
     * @param type            Certificate type
     * @param certificateData PEM-encoded certificate
     * @return Installation status
     */
    public CertificateStatus installCertificate(
            StationId stationId,
            CertificateType type,
            String certificateData) {

        logger.info("Installing {} certificate for station {}", type, stationId);

        try {
            // Parse and validate certificate
            X509Certificate x509Cert = parseCertificate(certificateData);

            // Extract certificate information
            String serialNumber = x509Cert.getSerialNumber().toString();
            String issuer = x509Cert.getIssuerDN().getName();
            LocalDateTime validFrom = x509Cert.getNotBefore().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            LocalDateTime validTo = x509Cert.getNotAfter().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            // Check if certificate is already expired
            if (LocalDateTime.now().isAfter(validTo)) {
                logger.warn("Certificate is already expired: {}", serialNumber);
                return CertificateStatus.REJECTED;
            }

            // Create and save certificate
            Certificate certificate = Certificate.create(
                    type,
                    certificateData,
                    serialNumber,
                    issuer,
                    validFrom,
                    validTo,
                    stationId);

            certificateRepository.save(certificate);

            logger.info("Certificate installed successfully: {}", serialNumber);
            return CertificateStatus.ACCEPTED;

        } catch (Exception e) {
            logger.error("Failed to install certificate for station {}: {}", stationId, e.getMessage());
            return CertificateStatus.FAILED;
        }
    }

    /**
     * Deletes a certificate by serial number.
     * 
     * @param serialNumber Certificate serial number
     * @return true if deleted, false if not found
     */
    public boolean deleteCertificate(String serialNumber) {
        logger.info("Deleting certificate with serial number: {}", serialNumber);

        // Find certificate by serial number (simplified - in production, use hash data)
        List<Certificate> allCertificates = certificateRepository
                .findByType(CertificateType.CHARGING_STATION_CERTIFICATE);

        for (Certificate cert : allCertificates) {
            if (cert.getSerialNumber() != null && cert.getSerialNumber().equals(serialNumber)) {
                certificateRepository.delete(cert.getId());
                logger.info("Certificate deleted: {}", serialNumber);
                return true;
            }
        }

        logger.warn("Certificate not found: {}", serialNumber);
        return false;
    }

    /**
     * Gets all installed certificates for a station.
     * 
     * @param stationId Station ID
     * @return List of certificates
     */
    public List<Certificate> getInstalledCertificates(StationId stationId) {
        return certificateRepository.findByStationId(stationId);
    }

    /**
     * Gets all installed certificates of a specific type for a station.
     * 
     * @param stationId Station ID
     * @param type      Certificate type
     * @return List of certificates
     */
    public List<Certificate> getInstalledCertificates(StationId stationId, CertificateType type) {
        return certificateRepository.findByStationIdAndType(stationId, type);
    }

    /**
     * Gets all valid (non-expired) certificates for a station.
     * 
     * @param stationId Station ID
     * @return List of valid certificates
     */
    public List<Certificate> getValidCertificates(StationId stationId) {
        return certificateRepository.findValidByStationId(stationId);
    }

    /**
     * Verifies if a certificate is valid.
     * 
     * @param certificateId Certificate ID
     * @return true if valid, false otherwise
     */
    public boolean verifyCertificate(CertificateId certificateId) {
        return certificateRepository.findById(certificateId)
                .map(Certificate::isValid)
                .orElse(false);
    }

    /**
     * Processes a signed certificate (CSR response).
     * 
     * @param stationId        Station ID
     * @param certificateChain PEM-encoded certificate chain
     * @return Processing status
     */
    public CertificateStatus processCertificateSigned(StationId stationId, String certificateChain) {
        logger.info("Processing signed certificate for station {}", stationId);

        try {
            // In a real implementation, this would:
            // 1. Parse the certificate chain
            // 2. Validate the chain
            // 3. Store each certificate in the chain
            // 4. Link them together

            // For now, simplified implementation
            return installCertificate(
                    stationId,
                    CertificateType.CHARGING_STATION_CERTIFICATE,
                    certificateChain);

        } catch (Exception e) {
            logger.error("Failed to process signed certificate: {}", e.getMessage());
            return CertificateStatus.FAILED;
        }
    }

    /**
     * Parses a PEM-encoded certificate.
     */
    private X509Certificate parseCertificate(String pemCertificate) throws Exception {
        // Remove PEM headers if present
        String certData = pemCertificate
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");

        byte[] certBytes = Base64.getDecoder().decode(certData);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}
