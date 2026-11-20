package com.casrusil.siierpai.modules.treasury.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.accounting.application.service.CashFlowProjectionService;
import com.casrusil.siierpai.modules.accounting.application.service.FinancialRatiosService;
import com.casrusil.siierpai.modules.treasury.application.service.CashManagementService;
import com.casrusil.siierpai.modules.treasury.domain.model.CashTransaction;
import com.casrusil.siierpai.modules.treasury.domain.port.in.TreasuryAiPredictorUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TreasuryCashEntriesEndpointTest {

    private final CashManagementService cashService = mock(CashManagementService.class);
    private final TreasuryController controller = new TreasuryController(
            cashService,
            mock(CashFlowProjectionService.class),
            mock(FinancialRatiosService.class),
            mock(TreasuryAiPredictorUseCase.class));

    @Test
    void getCashEntriesShouldListMovements() throws Exception {
        CompanyId companyId = CompanyId.random();
        when(cashService.listCashTransactions(any())).thenReturn(List.of(mock(CashTransaction.class)));

        var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(controller::listCashEntries);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void postCashEntriesPluralShouldRegisterMovement() throws Exception {
        CompanyId companyId = CompanyId.random();
        var request = new TreasuryController.CashEntryRequest(LocalDate.now(), "Venta caja",
                new BigDecimal("1000"), "ref", "SALES_CASH", "CLP", BigDecimal.ONE);

        var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(() -> controller.recordCashEntryPlural(request));

        assertEquals(200, response.getStatusCode().value());
        verify(cashService).recordCashTransaction(any(CashTransaction.class));
    }
}
