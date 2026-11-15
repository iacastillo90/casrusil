package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.application.service.*;
import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO;
import com.casrusil.siierpai.modules.accounting.domain.model.*;
import com.casrusil.siierpai.modules.accounting.domain.service.AnomalyDetectionService;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.accounting.infrastructure.web.dto.ComplianceDashboardDTO;
import com.casrusil.siierpai.modules.accounting.infrastructure.web.dto.ReconciliationStatsDTO;
import com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest.dto.GreenPassportDTO;
import com.casrusil.siierpai.modules.sustainability.domain.port.out.GreenMetricsRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.math.RoundingMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.casrusil.siierpai.modules.compliance.application.service.AcuseReciboScheduler;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.repository.TaxParameterJpaRepository;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.entity.TaxParameterEntity;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

/**
 * 🧠 FERRARI ENGINE PHASE 4: Centro de Inteligencia Unificado.
 * 
 * <p>
 * NOTA: Este controlador ha sido refactorizado para removir dependencias del
 * banking module (banking module fue removido para evitar conflicto de entidades
 * con bankingsync module). Las estadísticas de conciliación ahora son mock
 * hasta que se implemente un nuevo servicio de conciliación.
 * 
 * @since 2.0 - Ferrari Engine Refactoring
 */
@RestController
@RequestMapping("/api/v1/compliance")
@Tag(name = "Compliance Center", description = "Centro de Inteligencia Financiera y Tributaria")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','AUDITOR')")
public class ComplianceController {

    private final SiiAuditorService siiAuditorService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final F29CalculatorService f29CalculatorService;
    private final TaxAuditService taxAuditService;
    private final GreenMetricsRepository greenMetricsRepository;
    private final BalanceSheetService balanceSheetService;
    private final AcuseReciboScheduler acuseScheduler;
    private final TaxParameterJpaRepository taxRepo;
    private final com.casrusil.siierpai.modules.accounting.domain.service.FinancialHealthService financialHealthService;

    public ComplianceController(
            SiiAuditorService siiAuditorService,
            AnomalyDetectionService anomalyDetectionService,
            F29CalculatorService f29CalculatorService,
            TaxAuditService taxAuditService,
            GreenMetricsRepository greenMetricsRepository,
            BalanceSheetService balanceSheetService,
            AcuseReciboScheduler acuseScheduler,
            TaxParameterJpaRepository taxRepo,
            com.casrusil.siierpai.modules.accounting.domain.service.FinancialHealthService financialHealthService) {
        this.siiAuditorService = siiAuditorService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.f29CalculatorService = f29CalculatorService;
        this.taxAuditService = taxAuditService;
        this.greenMetricsRepository = greenMetricsRepository;
        this.balanceSheetService = balanceSheetService;
        this.acuseScheduler = acuseScheduler;
        this.taxRepo = taxRepo;
        this.financialHealthService = financialHealthService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "🧠 Obtiene el Dashboard Unificado Ferrari Engine")
    public ResponseEntity<ComplianceDashboardDTO> getComplianceDashboard(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam(value = "period", required = false) YearMonth period) {

        if (period == null)
            period = YearMonth.now();
        final YearMonth selectedPeriod = period;
        final CompanyId companyId = CompanyId.fromString(tenantId);

        // Ejecución paralela con Virtual Threads (Java 21+)
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            var siiTask = scope.fork(() -> siiAuditorService.compareWithSii(
                    companyId, selectedPeriod.getMonthValue(), selectedPeriod.getYear()));

            var f29Task = scope.fork(() -> f29CalculatorService.calculateDraftF29(
                    companyId, selectedPeriod));

            var taxTask = scope.fork(() -> taxAuditService.performAudit(
                    companyId, selectedPeriod.getYear(),
                    selectedPeriod.getMonthValue(), java.util.Collections.emptyList()));

            scope.join();
            scope.throwIfFailed();

            SiiAuditReportDTO siiReport = (SiiAuditReportDTO) siiTask.get();
            DraftF29 draftF29 = (DraftF29) f29Task.get();
            F29Report f29Report = draftF29.report();
            TaxAuditReport taxReport = (TaxAuditReport) taxTask.get();

            // 🆕 FERRARI ENGINE: Detección completa de anomalías
            List<AnomalyWarning> anomalies = anomalyDetectionService.detectAllAnomalies(
                    companyId, selectedPeriod, f29Report);

            // 🆕 FERRARI ENGINE: Green Passport
            GreenPassportDTO greenPassport = calculateGreenPassport(companyId, selectedPeriod);

            // 🆕 FERRARI ENGINE: Bank Reconciliation Stats (mockeado - banking module removido)
            // El banking module fue removido para evitar conflicto de entidades con bankingsync.
            // Las estadísticas de conciliación deben implementarse con un nuevo servicio.
            int totalTxns = 100;  // Mock - replaced banking module
            int reconciledCount = 85;
            int pendingCount = 15;
            double reconRate = 0.85;
            ReconciliationStatsDTO reconciliationStats = new ReconciliationStatsDTO(
                    totalTxns, reconciledCount, pendingCount, reconRate, 0.88);

            List<AnomalyWarning> acuseAlerts = acuseScheduler.getPendingAlerts(companyId);
            TaxParameterEntity taxParams = taxRepo.findById("YEAR_2024").orElse(null);

            return ResponseEntity.ok(new ComplianceDashboardDTO(
                    siiReport,
                    f29Report,
                    taxReport,
                    anomalies,
                    greenPassport,
                    reconciliationStats,
                    calculateHealthScore(siiReport, anomalies, greenPassport),
                    acuseAlerts,
                    taxParams));
        } catch (Exception e) {
            throw new RuntimeException("Error generando Dashboard de Compliance", e);
        }
    }

    /**
     * 🆕 FERRARI ENGINE: Calcula el Green Passport.
     */
    private GreenPassportDTO calculateGreenPassport(CompanyId companyId, YearMonth period) {
        BigDecimal carbonFootprint = greenMetricsRepository.calculateTotalCarbonFootprint(companyId);
        BigDecimal greenScore = greenMetricsRepository.calculateTotalGreenScore(companyId);
        long greenSuppliers = greenMetricsRepository.countGreenSuppliers(companyId);

        // Calcular salud ambiental (0-100)
        BigDecimal environmentalHealth = greenScore.divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP)
                .subtract(carbonFootprint.divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP))
                .add(new BigDecimal("50"));

        // Clamp entre 0 y 100
        if (environmentalHealth.compareTo(BigDecimal.ZERO) < 0) {
            environmentalHealth = BigDecimal.ZERO;
        }
        if (environmentalHealth.compareTo(new BigDecimal("100")) > 0) {
            environmentalHealth = new BigDecimal("100");
        }

        BalanceSheetReport balanceSheet = balanceSheetService.generateBalanceSheet(companyId, period.atEndOfMonth());

        // Salud financiera real y centralizada (misma fórmula que Sustainability).
        BigDecimal financialHealth = financialHealthService.calculateFromBalance(balanceSheet);

        String riskLevel = calculateRiskLevel(financialHealth, environmentalHealth);

        return new GreenPassportDTO(
                financialHealth,
                environmentalHealth,
                carbonFootprint,
                greenSuppliers,
                riskLevel);
    }

    private String calculateRiskLevel(BigDecimal financialHealth, BigDecimal environmentalHealth) {
        BigDecimal minHealth = financialHealth.min(environmentalHealth);
        if (minHealth.compareTo(new BigDecimal("70")) >= 0) {
            return "LOW";
        } else if (minHealth.compareTo(new BigDecimal("40")) >= 0) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    /**
     * 🆕 FERRARI ENGINE: Score global mejorado con sostenibilidad.
     */
    private int calculateHealthScore(SiiAuditReportDTO sii, List<AnomalyWarning> anomalies,
            GreenPassportDTO greenPassport) {
        int score = 100;

        // Penalización por desbalance SII
        if (!sii.isMatch())
            score -= 20;

        // Penalización por anomalías
        long criticalAnomalies = anomalies.stream()
                .filter(a -> a.severity() == AnomalyWarning.Severity.CRITICAL)
                .count();
        score -= (int) (criticalAnomalies * 10);
        score -= (anomalies.size() * 3);

        // Bonificación por sostenibilidad
        if (greenPassport.environmentalHealth().compareTo(new BigDecimal("70")) >= 0) {
            score += 5;
        }

        return Math.max(0, Math.min(100, score));
    }
}