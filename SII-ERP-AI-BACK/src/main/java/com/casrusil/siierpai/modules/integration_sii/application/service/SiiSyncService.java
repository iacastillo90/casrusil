package com.casrusil.siierpai.modules.integration_sii.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.DteSendResult;
import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncResult;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncState;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncStatusValue;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.AuthenticateSiiUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.DownloadRcvUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.SiiDteUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.SiiSyncUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiSyncStateRepository;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.integration_sii.domain.service.DteSenderService;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.modules.sso.domain.exception.CertificateNotFoundException;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.CompanyCertificate;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyCertificateRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service that reports SII integration health and drives
 * on-demand synchronization and DTE submission.
 *
 * <p>The synchronization is intentionally shallow: it refreshes the SII token,
 * validates RCV connectivity for the current month and records the outcome.
 * RCV rows are not persisted as invoices.</p>
 */
@Service
public class SiiSyncService implements SiiSyncUseCase, SiiDteUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SiiSyncService.class);
    private static final int MAX_ERROR_LENGTH = 500;

    private final SiiTokenRepository tokenRepository;
    private final SiiSyncStateRepository syncStateRepository;
    private final CafRepository cafRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final CompanyCertificateRepository certificateRepository;
    private final com.casrusil.siierpai.modules.sso.application.service.CompanyCertificateService companyCertificateService;
    private final AuthenticateSiiUseCase authenticateSiiUseCase;
    private final DownloadRcvUseCase downloadRcvUseCase;
    private final Pkcs12Handler pkcs12Handler;
    private final DteSenderService dteSenderService;

    @Value("${sii.certificate.path:}")
    private String defaultCertPath;

    @Value("${sii.certificate.password:}")
    private String defaultCertPassword;

    public SiiSyncService(
            SiiTokenRepository tokenRepository,
            SiiSyncStateRepository syncStateRepository,
            CafRepository cafRepository,
            InvoiceRepository invoiceRepository,
            CompanyRepository companyRepository,
            CompanyCertificateRepository certificateRepository,
            com.casrusil.siierpai.modules.sso.application.service.CompanyCertificateService companyCertificateService,
            AuthenticateSiiUseCase authenticateSiiUseCase,
            DownloadRcvUseCase downloadRcvUseCase,
            Pkcs12Handler pkcs12Handler,
            DteSenderService dteSenderService) {
        this.tokenRepository = tokenRepository;
        this.syncStateRepository = syncStateRepository;
        this.cafRepository = cafRepository;
        this.invoiceRepository = invoiceRepository;
        this.companyRepository = companyRepository;
        this.certificateRepository = certificateRepository;
        this.companyCertificateService = companyCertificateService;
        this.authenticateSiiUseCase = authenticateSiiUseCase;
        this.downloadRcvUseCase = downloadRcvUseCase;
        this.pkcs12Handler = pkcs12Handler;
        this.dteSenderService = dteSenderService;
    }

    @Override
    public SiiSyncState getStatus(CompanyId companyId) {
        return syncStateRepository.findByCompanyId(companyId)
                .map(stored -> new SiiSyncState(
                        companyId,
                        stored.lastSyncAt(),
                        stored.nextSyncAt(),
                        deriveSiiStatus(companyId),
                        deriveCafStatus(companyId),
                        deriveDteStatus(companyId),
                        stored.lastError(),
                        stored.updatedAt()))
                .orElseGet(() -> deriveState(companyId));
    }

    @Override
    public SiiSyncResult synchronize(CompanyId companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        String period = YearMonth.now().toString();

        try {
            SiiToken token = ensureValidToken(companyId);
            List<RcvData> purchases = downloadRcvUseCase.downloadPurchaseRegister(
                    token, companyId, company.getRut(), period);
            List<RcvData> sales = downloadRcvUseCase.downloadSalesRegister(
                    token, companyId, company.getRut(), period);

            LocalDateTime now = LocalDateTime.now();
            SiiSyncState state = new SiiSyncState(
                    companyId,
                    now,
                    now.plusHours(24),
                    SiiSyncStatusValue.SYNCED,
                    deriveCafStatus(companyId),
                    deriveDteStatus(companyId),
                    null,
                    now);
            syncStateRepository.save(state);

            logger.info("SII sync completed for company {}: {} purchases, {} sales (period {})",
                    companyId, purchases.size(), sales.size(), period);
            return new SiiSyncResult(true,
                    "SII synchronization completed for period " + period
                            + " (purchases: " + purchases.size() + ", sales: " + sales.size() + "). "
                            + "RCV documents were validated but not imported.");
        } catch (RuntimeException ex) {
            String detail = describe(ex);
            logger.warn("SII sync failed for company {}: {}", companyId, detail);
            persistFailure(companyId, detail);
            return new SiiSyncResult(false, detail);
        }
    }

    @Override
    public Invoice getDte(CompanyId companyId, String tipo, String folio) {
        InvoiceType type = resolveType(tipo);
        Long folioNumber = parseFolio(folio);

        return invoiceRepository.findFirstByCompanyIdAndTypeCodeAndFolio(companyId, type.getCode(), folioNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DTE " + type.getCode() + "/" + folioNumber + " not found"));
    }

    @Override
    public DteSendResult sendDte(CompanyId companyId, String dteId) {
        UUID invoiceId = parseInvoiceId(dteId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .filter(found -> found.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("DTE not found: " + dteId));

        DteSendResult result = dteSenderService.sendInvoice(invoice, companyId);
        if (result.success()) {
            invoice.markAsSent(result.trackId(), LocalDateTime.now());
            invoiceRepository.save(invoice);
        } else {
            persistFailure(companyId, result.message());
        }
        return result;
    }

    private SiiToken ensureValidToken(CompanyId companyId) {
        Optional<SiiToken> stored = tokenRepository.findByCompanyId(companyId);
        if (stored.isPresent() && stored.get().isValid()) {
            return stored.get();
        }

        SiiCertificate certificate = loadCertificate(companyId);
        SiiToken token = authenticateSiiUseCase.authenticate(certificate);
        tokenRepository.save(companyId, token);
        return token;
    }

    private SiiCertificate loadCertificate(CompanyId companyId) {
        Optional<SiiCertificate> stored = companyCertificateService.load(companyId);
        if (stored.isPresent()) {
            return stored.get();
        }

        if (defaultCertPath != null && !defaultCertPath.isBlank()) {
            logger.warn("Using default fallback certificate for company {} (not configured in DB)", companyId);
            return pkcs12Handler.loadCertificate(defaultCertPath, defaultCertPassword);
        }

        throw new CertificateNotFoundException("No SII certificate found for company " + companyId);
    }

    private SiiSyncState deriveState(CompanyId companyId) {
        LocalDateTime now = LocalDateTime.now();
        return new SiiSyncState(
                companyId,
                now,
                now.plusHours(24),
                deriveSiiStatus(companyId),
                deriveCafStatus(companyId),
                deriveDteStatus(companyId),
                null,
                now);
    }

    private SiiSyncStatusValue deriveSiiStatus(CompanyId companyId) {
        boolean validToken = tokenRepository.findByCompanyId(companyId)
                .map(SiiToken::isValid)
                .orElse(false);
        return validToken ? SiiSyncStatusValue.SYNCED : SiiSyncStatusValue.PENDING;
    }

    private SiiSyncStatusValue deriveCafStatus(CompanyId companyId) {
        return cafRepository.existsActiveByCompanyId(companyId)
                ? SiiSyncStatusValue.SYNCED
                : SiiSyncStatusValue.PENDING;
    }

    private SiiSyncStatusValue deriveDteStatus(CompanyId companyId) {
        return invoiceRepository.existsByCompanyIdAndOrigin(companyId, Invoice.ORIGIN_SII)
                ? SiiSyncStatusValue.SYNCED
                : SiiSyncStatusValue.PENDING;
    }

    private void persistFailure(CompanyId companyId, String detail) {
        LocalDateTime now = LocalDateTime.now();
        SiiSyncState previous = syncStateRepository.findByCompanyId(companyId).orElseGet(() -> deriveState(companyId));
        SiiSyncState failure = new SiiSyncState(
                companyId,
                previous.lastSyncAt() != null ? previous.lastSyncAt() : now,
                previous.nextSyncAt() != null ? previous.nextSyncAt() : now.plusHours(24),
                deriveSiiStatus(companyId),
                deriveCafStatus(companyId),
                deriveDteStatus(companyId),
                truncate(detail),
                now);
        syncStateRepository.save(failure);
    }

    private InvoiceType resolveType(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("DTE type is required");
        }
        String value = tipo.trim();
        try {
            return InvoiceType.fromCode(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            try {
                return InvoiceType.valueOf(value);
            } catch (IllegalArgumentException invalidName) {
                throw new IllegalArgumentException("Invalid DTE type: " + tipo);
            }
        }
    }

    private Long parseFolio(String folio) {
        if (folio == null || folio.isBlank()) {
            throw new IllegalArgumentException("Folio is required");
        }
        try {
            return Long.parseLong(folio.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid folio: " + folio);
        }
    }

    private UUID parseInvoiceId(String dteId) {
        if (dteId == null || dteId.isBlank()) {
            throw new IllegalArgumentException("Invoice id is required");
        }
        try {
            return UUID.fromString(dteId.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid invoice id: " + dteId);
        }
    }

    private String describe(RuntimeException ex) {
        String message = ex.getMessage();
        return message != null && !message.isBlank() ? message : ex.getClass().getSimpleName();
    }

    private String truncate(String message) {
        if (message == null || message.length() <= MAX_ERROR_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_LENGTH);
    }
}
