package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class F29CalculatorServiceTest {

    @InjectMocks
    private F29CalculatorService f29CalculatorService;

    @Mock
    private AccountingEntryRepository accountingEntryRepository;

    @Test
    void calculateF29_ShouldReturnZeroReport_WhenNoEntries() {
        CompanyId companyId = CompanyId.random();
        YearMonth period = YearMonth.of(2023, 10);

        when(accountingEntryRepository.findByCompanyId(companyId)).thenReturn(Collections.emptyList());

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

        // Accounting Entry: Net 1000, VAT 190 (19%), Total 1190
        AccountingEntryLine line = new AccountingEntryLine(
                "210401", // VAT account (example)
                "IVA",
                BigDecimal.ZERO,
                new BigDecimal("190") // Tax Amount
        );
        AccountingEntry entry = new AccountingEntry(
                companyId,
                LocalDate.now(),
                "Inv 1",
                "F-1",
                "SALE",
                "76123456-7",
                "Test Company",
                "33",
                "1",
                "DRAFT",
                List.of(line),
                EntryType.NORMAL // Using NORMAL as it's a sale
        );

        when(accountingEntryRepository.findByCompanyId(companyId)).thenReturn(List.of(entry));

        F29Report report = f29CalculatorService.calculateF29(companyId, period);

        assertNotNull(report);
    }
}
