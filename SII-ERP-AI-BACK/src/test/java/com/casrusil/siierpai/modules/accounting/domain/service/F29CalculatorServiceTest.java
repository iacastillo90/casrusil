package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.entity.TaxParameterEntity;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.repository.TaxParameterJpaRepository;
import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.modules.fees.domain.port.out.FeeReceiptRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class F29CalculatorServiceTest {

    @InjectMocks
    private F29CalculatorService f29CalculatorService;

    @Mock
    private AccountingEntryRepository accountingEntryRepository;

    @Mock
    private FeeReceiptRepository feeReceiptRepository;

    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @Mock
    private TaxParameterJpaRepository taxParameterRepository;

    @Test
    void calculateF29_ShouldReturnZeroReport_WhenNoEntries() {
        CompanyId companyId = CompanyId.random();
        YearMonth period = YearMonth.of(2023, 10);

        when(accountingEntryRepository.findByCompanyIdAndEntryDateBetween(
                companyId, period.atDay(1), period.atEndOfMonth())).thenReturn(List.of());
        when(feeReceiptRepository.findByCompanyIdAndIssueDateBetween(
                companyId, period.atDay(1), period.atEndOfMonth())).thenReturn(List.of());

        F29Report report = f29CalculatorService.calculateF29(companyId, period);

        assertNotNull(report);
        assertEquals(BigDecimal.ZERO, report.vatDebit());
        assertEquals(BigDecimal.ZERO, report.vatCredit());
        assertEquals(BigDecimal.ZERO, report.vatPayable());
        assertEquals(BigDecimal.ZERO, report.totalSalesTaxable());
        assertEquals(BigDecimal.ZERO, report.totalSalesExempt());
    }

    @Test
    void calculateF29_ShouldCalculateCorrectly_WhenEntriesExist() {
        CompanyId companyId = CompanyId.random();
        YearMonth period = YearMonth.of(2023, 10);

        // Venta de 1000 + IVA 190 = 1190, asiento balanceado.
        AccountingEntry entry = new AccountingEntry(
                companyId,
                period.atDay(15),
                "Venta F-1",
                "F-1",
                "INVOICE",
                "76123456-7",
                "Test Company",
                "33",
                "1",
                "POSTED",
                List.of(
                        AccountingEntryLine.debit("110501", "Caja", new BigDecimal("1190")),
                        AccountingEntryLine.credit("410101", "Ventas", new BigDecimal("1000")),
                        AccountingEntryLine.credit("210401", "IVA Débito", new BigDecimal("190"))),
                EntryType.SALE);

        when(accountingEntryRepository.findByCompanyIdAndEntryDateBetween(
                companyId, period.atDay(1), period.atEndOfMonth())).thenReturn(List.of(entry));
        when(feeReceiptRepository.findByCompanyIdAndIssueDateBetween(
                companyId, period.atDay(1), period.atEndOfMonth())).thenReturn(List.of());

        F29Report report = f29CalculatorService.calculateF29(companyId, period);

        assertNotNull(report);
        assertEquals(new BigDecimal("1000"), report.totalSalesTaxable());
        assertEquals(new BigDecimal("190"), report.vatDebit());
        assertEquals(new BigDecimal("190"), report.vatPayable());
    }

    @Test
    void calculateF29_ShouldUseBheRateFromTaxParametersForEachYear() {
        CompanyId companyId = CompanyId.random();
        YearMonth period2024 = YearMonth.of(2024, 6);
        YearMonth period2025 = YearMonth.of(2025, 6);

        TaxParameterEntity params2024 = new TaxParameterEntity();
        params2024.setId("YEAR_2024");
        params2024.setBheRetentionRate(new BigDecimal("13.75"));

        TaxParameterEntity params2025 = new TaxParameterEntity();
        params2025.setId("YEAR_2025");
        params2025.setBheRetentionRate(new BigDecimal("15.00"));

        FeeReceipt fee2024 = FeeReceipt.create(companyId, 1L, "11111111-1", "22222222-2", "Prestador 2024",
                period2024.atDay(10), new BigDecimal("10000"), new BigDecimal("1375"), new BigDecimal("8625"));
        FeeReceipt fee2025 = FeeReceipt.create(companyId, 2L, "11111111-1", "22222222-2", "Prestador 2025",
                period2025.atDay(10), new BigDecimal("10000"), new BigDecimal("1500"), new BigDecimal("8500"));

        when(accountingEntryRepository.findByCompanyIdAndEntryDateBetween(
                org.mockito.ArgumentMatchers.eq(companyId),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        when(feeReceiptRepository.findByCompanyIdAndIssueDateBetween(
                org.mockito.ArgumentMatchers.eq(companyId),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(fee2024))
                .thenReturn(List.of(fee2025));
        when(taxParameterRepository.findById("YEAR_2024")).thenReturn(Optional.of(params2024));
        when(taxParameterRepository.findById("YEAR_2025")).thenReturn(Optional.of(params2025));

        F29Report report2024 = f29CalculatorService.calculateF29(companyId, period2024);
        F29Report report2025 = f29CalculatorService.calculateF29(companyId, period2025);

        assertEquals(new BigDecimal("1375"), report2024.feeWithholding());
        assertEquals(new BigDecimal("1500"), report2025.feeWithholding());
        assertTrue(report2024.details().stream()
                .anyMatch(line -> line.code().equals("151") && line.description().contains("13.75")));
        assertTrue(report2025.details().stream()
                .anyMatch(line -> line.code().equals("151") && line.description().contains("15.00")));

        verify(taxParameterRepository).findById("YEAR_2024");
        verify(taxParameterRepository).findById("YEAR_2025");
    }
}
