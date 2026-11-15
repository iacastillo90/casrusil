package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para métricas del dashboard financiero.
 * 
 * <p>Este controlador expone KPIs pre-calculados para el frontend,
 * evitando que tenga que cargar todas las facturas y calcularlos localmente.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code GET /api/v1/dashboard/finance/metrics}: KPIs financieros pre-calculados</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/dashboard/finance")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "KPIs y métricas financieras del dashboard")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','SALES')")
public class FinanceDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceDashboardController.class);

    private final SearchInvoicesUseCase searchInvoicesUseCase;

    @GetMapping("/metrics")
    @Operation(summary = "Get dashboard metrics", 
               description = "Returns pre-calculated KPIs for the financial dashboard")
    public ResponseEntity<DashboardMetricsDto> getDashboardMetrics() {
        logger.info("Fetching dashboard metrics");

        String companyId = CompanyContext.requireCompanyId().getValue().toString();
        List<Invoice> invoices = searchInvoicesUseCase.getInvoicesByCompany(
                com.casrusil.siierpai.shared.domain.valueobject.CompanyId.fromString(companyId));
        
        DashboardMetricsDto metrics = DashboardMetricsDto.from(invoices);
        
        return ResponseEntity.ok(metrics);
    }
}