package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.application.service.ReportingService;
import com.casrusil.siierpai.modules.accounting.application.service.WeeklyReportService;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29SenderService;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.casrusil.siierpai.shared.infrastructure.reporting.ExcelExportService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Deuda técnica: {@code POST /reports/f29/submit} simulaba el envío al SII y
 * respondía {@code SUBMITTED} con un folio inventado. Ahora responde 501 y no
 * informa un envío que no ocurrió.
 */
class ReportingControllerF29SubmitTest {

    @Test
    void submitF29_respondeNotImplementedSinFolioInventado() {
        CompanyId company = new CompanyId(UUID.randomUUID());
        YearMonth period = YearMonth.of(2026, 10);

        F29CalculatorService calculator = mock(F29CalculatorService.class);
        F29Report report = mock(F29Report.class);
        when(report.period()).thenReturn(period);
        when(report.vatPayable()).thenReturn(new BigDecimal("190000"));
        when(calculator.calculateF29(company, period)).thenReturn(report);

        ReportingController controller = new ReportingController(
                mock(SearchInvoicesUseCase.class),
                mock(ExcelExportService.class),
                mock(BalanceSheetService.class),
                calculator,
                new F29SenderService(),
                mock(ReportingService.class),
                mock(WeeklyReportService.class));

        AtomicReference<ResponseEntity<Map<String, String>>> response = new AtomicReference<>();
        CompanyContext.runInCompanyContext(company, () -> response.set(controller.submitF29("2026-10")));

        assertThat(response.get().getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(response.get().getBody())
                .containsEntry("status", "NOT_IMPLEMENTED")
                .containsEntry("period", "2026-10")
                .containsEntry("amountToPay", "190000")
                .doesNotContainKey("transactionId");
        assertThat(response.get().getBody().get("message")).contains("sii.cl");
    }
}
