package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.application.service.SiiAuditorService;
import com.casrusil.siierpai.modules.accounting.application.service.TaxAuditService;
import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO;
import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.modules.accounting.domain.model.DraftF29;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.model.TaxAuditReport;
import com.casrusil.siierpai.modules.accounting.domain.service.AnomalyDetectionService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.service.FinancialHealthService;
import com.casrusil.siierpai.modules.compliance.application.service.AcuseReciboScheduler;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.repository.TaxParameterJpaRepository;
import com.casrusil.siierpai.modules.sustainability.domain.port.out.GreenMetricsRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComplianceControllerTest {

    @Test
    void dashboardShouldUseCompanyContextWithoutTenantHeader() throws Exception {
        CompanyId companyId = CompanyId.random();
        YearMonth period = YearMonth.of(2026, 1);

        SiiAuditorService siiAuditor = mock(SiiAuditorService.class);
        AnomalyDetectionService anomalies = mock(AnomalyDetectionService.class);
        F29CalculatorService f29 = mock(F29CalculatorService.class);
        TaxAuditService taxAudit = mock(TaxAuditService.class);
        GreenMetricsRepository green = mock(GreenMetricsRepository.class);
        BalanceSheetService balance = mock(BalanceSheetService.class);
        AcuseReciboScheduler acuse = mock(AcuseReciboScheduler.class);
        TaxParameterJpaRepository taxRepo = mock(TaxParameterJpaRepository.class);
        FinancialHealthService health = mock(FinancialHealthService.class);

        when(siiAuditor.compareWithSii(any(), anyInt(), anyInt())).thenReturn(new SiiAuditReportDTO(
                new SiiAuditReportDTO.TaxSummary(BigDecimal.ZERO, BigDecimal.ZERO, 0),
                new SiiAuditReportDTO.TaxSummary(BigDecimal.ZERO, BigDecimal.ZERO, 0),
                List.of(), true));
        when(f29.calculateDraftF29(any(), any())).thenReturn(new DraftF29(
                new F29Report(period, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, List.of(), List.of()),
                List.of(), true));
        when(taxAudit.performAudit(any(), anyInt(), anyInt(), anyList()))
                .thenReturn(new TaxAuditReport(null, List.of()));
        when(anomalies.detectAllAnomalies(any(), any(), any())).thenReturn(List.of());
        when(green.calculateTotalCarbonFootprint(any())).thenReturn(BigDecimal.ZERO);
        when(green.calculateTotalGreenScore(any())).thenReturn(BigDecimal.ZERO);
        when(green.countGreenSuppliers(any())).thenReturn(0L);
        when(balance.generateBalanceSheet(any(), any())).thenReturn(new BalanceSheetReport(
                LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                Map.of(), Map.of(), Map.of(), true));
        when(health.calculateFromBalance(any())).thenReturn(new BigDecimal("50"));
        when(acuse.getPendingAlerts(any())).thenReturn(List.of());
        when(taxRepo.findById(any())).thenReturn(Optional.empty());

        ComplianceController controller = new ComplianceController(siiAuditor, anomalies, f29, taxAudit, green,
                balance, acuse, taxRepo, health);

        var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> controller.getComplianceDashboard(period));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(100, response.getBody().globalHealthScore());
    }
}
