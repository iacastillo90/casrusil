package com.casrusil.siierpai.modules.treasury.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.accounting.application.service.CashFlowProjectionService;
import com.casrusil.siierpai.modules.accounting.application.service.FinancialRatiosService;
import com.casrusil.siierpai.modules.treasury.application.service.CashManagementService;
import com.casrusil.siierpai.modules.treasury.domain.model.CashTransaction;
import com.casrusil.siierpai.modules.treasury.domain.port.in.TreasuryAiPredictorUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/treasury")
@PreAuthorize("hasAnyRole('ADMIN','TREASURER','ACCOUNTANT')")
public class TreasuryController {

    private final CashManagementService cashManagementService;
    private final CashFlowProjectionService cashFlowProjectionService;
    private final FinancialRatiosService financialRatiosService;
    private final TreasuryAiPredictorUseCase aiPredictorUseCase;

    public TreasuryController(CashManagementService cashManagementService,
            CashFlowProjectionService cashFlowProjectionService,
            FinancialRatiosService financialRatiosService,
            TreasuryAiPredictorUseCase aiPredictorUseCase) {
        this.cashManagementService = cashManagementService;
        this.cashFlowProjectionService = cashFlowProjectionService;
        this.financialRatiosService = financialRatiosService;
        this.aiPredictorUseCase = aiPredictorUseCase;
    }

    // ✅ Endpoint existente
    @GetMapping("/cash-flow-daily")
    public ResponseEntity<CashFlowProjectionService.DailyCashFlowReport> getDailyCashFlow(
            @RequestParam int year,
            @RequestParam int month) {

        CompanyId companyId = CompanyContext.requireCompanyId();
        var report = cashFlowProjectionService.getDailyCashFlow(companyId, year, month);

        return ResponseEntity.ok(report);
    }

    // ✅ NUEVO ENDPOINT: Ratios Financieros
    @GetMapping("/financial-ratios")
    public ResponseEntity<FinancialRatiosService.LiquidityReport> getFinancialRatios(
            @RequestParam int year,
            @RequestParam int month) {

        CompanyId companyId = CompanyContext.requireCompanyId();
        var report = financialRatiosService.getLiquidityRatios(companyId, year, month);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/ai-forecast")
    public ResponseEntity<TreasuryAiPredictorUseCase.AiForecastReport> getAiForecast(
            @RequestParam(defaultValue = "30") int days) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(aiPredictorUseCase.predictCashFlow(companyId, days));
    }

    /**
     * Registra un movimiento de caja (ruta histórica en singular).
     *
     * @deprecated el front usa {@code POST /treasury/cash-entries}. Se mantiene
     *             solo por compatibilidad con clientes antiguos; no agregar usos
     *             nuevos.
     */
    @Deprecated
    @PostMapping("/cash-entry")
    public ResponseEntity<Void> recordCashEntry(@Valid @RequestBody CashEntryRequest request) {
        cashManagementService.recordCashTransaction(toCashTransaction(request));
        return ResponseEntity.ok().build();
    }

    /**
     * Lista los movimientos de caja de la empresa.
     *
     * @return movimientos ordenados por fecha descendente
     */
    @GetMapping("/cash-entries")
    public ResponseEntity<List<CashTransaction>> listCashEntries() {
        return ResponseEntity.ok(cashManagementService.listCashTransactions(CompanyContext.requireCompanyId()));
    }

    /**
     * Registra un movimiento de caja. Contrato vigente (lo usa el front); el
     * endpoint histórico {@code /cash-entry} queda obsoleto y comparte la misma
     * lógica.
     *
     * @param request movimiento a registrar
     * @return 204
     */
    @PostMapping("/cash-entries")
    public ResponseEntity<Void> recordCashEntryPlural(@Valid @RequestBody CashEntryRequest request) {
        cashManagementService.recordCashTransaction(toCashTransaction(request));
        return ResponseEntity.ok().build();
    }

    private CashTransaction toCashTransaction(CashEntryRequest request) {
        return CashTransaction.create(
                CompanyContext.requireCompanyId(), request.date(), request.description(),
                request.amount(), request.reference(), request.category(),
                request.currency() != null ? request.currency() : "CLP",
                request.exchangeRate() != null ? request.exchangeRate() : BigDecimal.ONE);
    }

    public record CashEntryRequest(LocalDate date, String description, BigDecimal amount, String reference,
            String category, String currency, BigDecimal exchangeRate) {
    }
}
