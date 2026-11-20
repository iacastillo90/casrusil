package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportingServiceTest {

    private final AccountingEntryRepository entryRepo = mock(AccountingEntryRepository.class);
    private final AccountRepository accountRepo = mock(AccountRepository.class);
    private final AiFinancialAnalyst aiAnalyst = mock(AiFinancialAnalyst.class);
    private final ReportingService service = new ReportingService(entryRepo, accountRepo, aiAnalyst);

    @Test
    void incomeStatementShouldDegradeWhenAiFails() throws Exception {
        CompanyId companyId = CompanyId.random();
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        when(entryRepo.findInPeriodForClasses(any(), any(), any(), anyList())).thenReturn(List.of());
        when(accountRepo.findAll(any())).thenReturn(List.of());
        when(aiAnalyst.generateExecutiveSummary(any()))
                .thenThrow(new RuntimeException("Gemini no disponible"));

        var report = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> service.generateIncomeStatement(from, to));

        assertEquals(BigDecimal.ZERO, report.netIncome());
        assertTrue(report.aiAnalysis().contains("no disponible"), report.aiAnalysis());
    }
}
