package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.application.service.*;
import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO;
import com.casrusil.siierpai.modules.accounting.domain.model.*;
import com.casrusil.siierpai.modules.accounting.domain.service.AnomalyDetectionService;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.accounting.infrastructure.web.dto.ComplianceDashboardDTO;
import com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest.dto.GreenPassportDTO;
import com.casrusil.siierpai.modules.sustainability.domain.port.out.GreenMetricsRepository;
import com.casrusil.siierpai.modules.banking.infrastructure.web.dto.ReconciliationStatsDTO;
import com.casrusil.siierpai.modules.banking.application.service.BankReconciliationWorkbenchService;
import com.casrusil.siierpai.modules.banking.application.service.ReconciliationLearningService;
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
 * @since 2.0 - Ferrari Engine Phase 4
 */
@RestController
@RequestMapping("/api/v1/compliance")
@Tag(name = "Compliance Center", description = "Centro de Inteligencia Financiera y Tributaria")
public class ComplianceController {

    private final SiiAuditorService siiAuditorService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final F29CalculatorService f29CalculatorService;
    private final TaxAuditService taxAuditService;
    private final GreenMetricsRepository greenMetricsRepository;
    private final BalanceSheetService balanceSheetService;
    private final AcuseReciboScheduler acuseScheduler;
    private final TaxParameterJpaRepository taxRepo;
    private final BankReconciliationWorkbenchService bankWorkbench;
    private final ReconciliationLearningService learningService;

    public ComplianceController(
            SiiAuditorService siiAuditorService,
            AnomalyDetectionService anomalyDetectionService,
            F29CalculatorService f29CalculatorService,
            TaxAuditService taxAuditService,
            GreenMetricsRepository greenMetricsRepository,
            BalanceSheetService balanceSheetService,
            AcuseReciboScheduler acuseScheduler,
            TaxParameterJpaRepository taxRepo,
            BankReconciliationWorkbenchService bankWorkbench,
            ReconciliationLearningService learningService) {
        this.siiAuditorService = siiAuditorService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.f29CalculatorService = f29CalculatorService;
        this.taxAuditService = taxAuditService;
        this.greenMetricsRepository = greenMetricsRepository;
        this.balanceSheetService = balanceSheetService;
        this.acuseScheduler = acuseScheduler;
        this.taxRepo = taxRepo;
        this.bankWorkbench = bankWorkbench;
        this.learningService = learningService;
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

            // 🆕 FERRARI ENGINE: Bank Reconciliation Stats (mockeado por ahora)
            // Calcular estadísticas reales de conciliación bancaria
            var reconDashboard = bankWorkbench.getDashboard(companyId);
            int totalTxns = reconDashboard.unmatchedBankLines().size() + reconDashboard.suggestions().size();
            int reconciledCount = reconDashboard.suggestions().size();
            int pendingCount = reconDashboard.unmatchedBankLines().size();
            double reconRate = totalTxns > 0 ? (double) reconciledCount / totalTxns : 0.0;
            double avgConfidence = reconDashboard.suggestions().stream()
                .mapToDouble(m -> switch(m.confidenceLevel()) { case "HIGH" -> 0.9; case "MEDIUM" -> 0.65; default -> 0.4; })
                .average().orElse(0.0);
            double learningPrecision = learningService.calculatePrecision(companyId);
            ReconciliationStatsDTO reconciliationStats = new ReconciliationStatsDTO(
                    totalTxns, reconciledCount, pendingCount, reconRate, Math.max(avgConfidence, learningPrecision));

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
        
        BigDecimal financialHealth = new BigDecimal("75.0");
        if (balanceSheet.totalLiabilities().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = balanceSheet.totalAssets().divide(balanceSheet.totalLiabilities(), 2, RoundingMode.HALF_UP);
            financialHealth = ratio.multiply(new BigDecimal("50")).min(new BigDecimal("100"));
        } else if (balanceSheet.totalAssets().compareTo(BigDecimal.ZERO) > 0) {
            financialHealth = new BigDecimal("100");
        } else {
            financialHealth = new BigDecimal("50");
        }

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
