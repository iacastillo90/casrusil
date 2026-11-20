package com.casrusil.siierpai.modules.fees.application.service;

import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.modules.fees.domain.port.out.FeeReceiptRepository;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.casrusil.siierpai.modules.fees.domain.util.RetentionRateCalculator;

@Service
public class FeeReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(FeeReceiptService.class);

    private final FeeReceiptRepository feeReceiptRepository;
    private final FeeReceiptParser feeReceiptParser;
    private final CompanyRepository companyRepository;

    public FeeReceiptService(FeeReceiptRepository feeReceiptRepository, FeeReceiptParser feeReceiptParser,
            CompanyRepository companyRepository) {
        this.feeReceiptRepository = feeReceiptRepository;
        this.feeReceiptParser = feeReceiptParser;
        this.companyRepository = companyRepository;
    }

    /**
     * Lista las boletas de honorarios de la empresa, opcionalmente filtradas por
     * año de emisión. Resultado ordenado por fecha de emisión descendente y folio
     * descendente.
     *
     * @param companyId empresa (tenant) resuelta desde el JWT, nunca desde el request
     * @param year      año de emisión; {@code null} devuelve todos los registros
     * @return boletas de la empresa
     */
    @Transactional(readOnly = true)
    public List<FeeReceipt> listFees(CompanyId companyId, Integer year) {
        List<FeeReceipt> receipts = (year == null)
                ? feeReceiptRepository.findByCompanyId(companyId)
                : feeReceiptRepository.findByCompanyIdAndYear(companyId, year);

        Comparator<FeeReceipt> byIssueDateDesc = Comparator.comparing(
                FeeReceipt::getIssueDate, Comparator.nullsLast(Comparator.reverseOrder()));
        Comparator<FeeReceipt> byFolioDesc = Comparator.comparing(
                FeeReceipt::getFolio, Comparator.nullsLast(Comparator.reverseOrder()));

        return receipts.stream()
                .sorted(byIssueDateDesc.thenComparing(byFolioDesc))
                .toList();
    }

    @Transactional
    public ImportResult importFromCsv(InputStream inputStream, CompanyId companyId) {
        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));

            List<FeeReceipt> parsedReceipts = feeReceiptParser.parse(inputStream, companyId);

            // Enrich with Receiver Rut (My Company)
            List<FeeReceipt> finalReceipts = parsedReceipts.stream()
                    .map(r -> new FeeReceipt(
                            r.getId(),
                            r.getCompanyId(),
                            r.getFolio(),
                            r.getIssuerRut(),
                            company.getRut(), // Set correct receiver RUT
                            r.getIssuerName(),
                            r.getIssueDate(),
                            r.getGrossAmount(),
                            r.getRetentionAmount(),
                            r.getNetAmount(),
                            r.getStatus()))
                    .collect(Collectors.toList());

            // Validate Retention Rates
            for (FeeReceipt r : finalReceipts) {
                BigDecimal expectedRate = RetentionRateCalculator.getRate(Year.of(r.getIssueDate().getYear()));
                BigDecimal calculatedRetention = r.getGrossAmount().multiply(expectedRate).setScale(0,
                        RoundingMode.HALF_UP);

                // Allow small difference of 1-5 pesos due to rounding
                if (r.getRetentionAmount().subtract(calculatedRetention).abs().compareTo(new BigDecimal(5)) > 0) {
                    logger.warn("Retention mismatch for receipt {}: Expected {}, Got {}", r.getFolio(),
                            calculatedRetention, r.getRetentionAmount());
                    // In strict mode, we might reject. For now, we log warning.
                }
            }

            feeReceiptRepository.saveAll(finalReceipts);

            return new ImportResult(finalReceipts.size(), 0, "Import successful");
        } catch (Exception e) {
            logger.error("Error importing fee receipts", e);
            return new ImportResult(0, 0, e.getMessage());
        }
    }

    public record ImportResult(int successCount, int errorCount, String message) {
    }
}
