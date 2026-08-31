package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.energy.application.service.analytics.DemandForecastService;
import com.casrusil.siierpai.modules.energy.application.service.analytics.UsagePatternAnalyzer;
import com.casrusil.siierpai.modules.energy.application.service.reporting.EnergyReportService;
import com.casrusil.siierpai.modules.energy.application.service.reporting.RevenueReportService;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.reporting.EnergyReport;
import com.casrusil.siierpai.modules.energy.domain.model.reporting.ReportPeriod;
import com.casrusil.siierpai.modules.energy.domain.model.reporting.ReportType;
import com.casrusil.siierpai.modules.energy.domain.model.reporting.UsagePattern;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for energy reporting and analytics.
 */
@RestController
@RequestMapping("/api/v1/energy/reports")
@Tag(name = "Energy Reports & Analytics", description = "Energy consumption, revenue, and usage analytics")
public class EnergyReportingController {

        private static final Logger logger = LoggerFactory.getLogger(EnergyReportingController.class);

        private final EnergyReportService energyReportService;
        private final RevenueReportService revenueReportService;
        private final UsagePatternAnalyzer usagePatternAnalyzer;
        private final DemandForecastService demandForecastService;

        public EnergyReportingController(
                        EnergyReportService energyReportService,
                        RevenueReportService revenueReportService,
                        UsagePatternAnalyzer usagePatternAnalyzer,
                        DemandForecastService demandForecastService) {
                this.energyReportService = energyReportService;
                this.revenueReportService = revenueReportService;
                this.usagePatternAnalyzer = usagePatternAnalyzer;
                this.demandForecastService = demandForecastService;
        }

        // ==================== Energy Reports ====================

        @GetMapping("/energy")
        @Operation(summary = "Get energy consumption report", description = "Generate energy report for specified period")
        public ResponseEntity<EnergyReport> getEnergyReport(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                logger.info("Energy report requested: {} to {}", startDate, endDate);

                EnergyReport report = energyReportService.generateCustomReport(startDate, endDate);

                return ResponseEntity.ok(report);
        }

        @GetMapping("/energy/daily")
        @Operation(summary = "Get daily energy report")
        public ResponseEntity<EnergyReport> getDailyEnergyReport(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

                logger.info("Daily energy report requested for {}", date);

                EnergyReport report = energyReportService.generateDailyReport(date);

                return ResponseEntity.ok(report);
        }

        @GetMapping("/energy/monthly")
        @Operation(summary = "Get monthly energy report")
        public ResponseEntity<EnergyReport> getMonthlyEnergyReport(
                        @RequestParam int year,
                        @RequestParam int month) {

                logger.info("Monthly energy report requested for {}-{}", year, month);

                EnergyReport report = energyReportService.generateMonthlyReport(year, month);

                return ResponseEntity.ok(report);
        }

        @GetMapping("/energy/export/csv")
        @Operation(summary = "Export energy report to CSV")
        public ResponseEntity<String> exportEnergyReportCsv(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                logger.info("CSV export requested: {} to {}", startDate, endDate);

                EnergyReport report = energyReportService.generateCustomReport(startDate, endDate);
                String csv = energyReportService.exportToCSV(report);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=energy-report.csv")
                                .contentType(MediaType.TEXT_PLAIN)
                                .body(csv);
        }

        // ==================== Revenue Reports ====================

        @GetMapping("/revenue")
        @Operation(summary = "Get revenue report", description = "Generate revenue report for specified period")
        public ResponseEntity<RevenueReportService.RevenueData> getRevenueReport(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                logger.info("Revenue report requested: {} to {}", startDate, endDate);

                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);

                RevenueReportService.RevenueData revenue = revenueReportService.calculateRevenue(start, end);

                return ResponseEntity.ok(revenue);
        }

        @GetMapping("/revenue/by-station")
        @Operation(summary = "Get revenue breakdown by station")
        public ResponseEntity<List<RevenueReportService.StationRevenue>> getRevenueByStation(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                logger.info("Revenue by station requested: {} to {}", startDate, endDate);

                List<RevenueReportService.StationRevenue> breakdown = revenueReportService.getRevenueByStation(
                                startDate,
                                endDate);

                return ResponseEntity.ok(breakdown);
        }

        @GetMapping("/revenue/profit-analysis")
        @Operation(summary = "Get profit analysis")
        public ResponseEntity<RevenueReportService.ProfitAnalysis> getProfitAnalysis(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                logger.info("Profit analysis requested: {} to {}", startDate, endDate);

                RevenueReportService.ProfitAnalysis analysis = revenueReportService.getProfitAnalysis(startDate,
                                endDate);

                return ResponseEntity.ok(analysis);
        }

        // ==================== Usage Analytics ====================

        @GetMapping("/usage-patterns")
        @Operation(summary = "Get usage pattern analysis", description = "Analyze usage patterns for a station")
        public ResponseEntity<UsagePattern> getUsagePatterns(
                        @RequestParam UUID stationId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                logger.info("Usage patterns requested for station {}: {} to {}",
                                stationId, startDate, endDate);

                UsagePattern pattern = usagePatternAnalyzer.analyzePatterns(
                                StationId.of(stationId),
                                startDate,
                                endDate);

                return ResponseEntity.ok(pattern);
        }

        // ==================== Demand Forecasting ====================

        @GetMapping("/forecast")
        @Operation(summary = "Get demand forecast", description = "Predict future charging demand")
        public ResponseEntity<DemandForecastService.DemandForecast> getDemandForecast(
                        @RequestParam UUID stationId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
                        @RequestParam int targetHour) {

                logger.info("Demand forecast requested for station {} on {} at hour {}",
                                stationId, targetDate, targetHour);

                DemandForecastService.DemandForecast forecast = demandForecastService.predictDemand(
                                StationId.of(stationId),
                                targetDate,
                                targetHour);

                return ResponseEntity.ok(forecast);
        }

        @GetMapping("/forecast/capacity-plan")
        @Operation(summary = "Get capacity planning report")
        public ResponseEntity<DemandForecastService.CapacityPlanningReport> getCapacityPlan(
                        @RequestParam UUID stationId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                logger.info("Capacity plan requested for station {}: {} to {}",
                                stationId, startDate, endDate);

                DemandForecastService.CapacityPlanningReport report = demandForecastService.generateCapacityPlan(
                                StationId.of(stationId),
                                startDate,
                                endDate);

                return ResponseEntity.ok(report);
        }

        @GetMapping("/forecast/load-balancing")
        @Operation(summary = "Get load balancing recommendation")
        public ResponseEntity<DemandForecastService.LoadBalancingRecommendation> getLoadBalancingRecommendation(
                        @RequestParam List<UUID> stationIds,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetTime) {

                logger.info("Load balancing recommendation requested for {} stations at {}",
                                stationIds.size(), targetTime);

                List<StationId> stations = stationIds.stream()
                                .map(StationId::of)
                                .toList();

                DemandForecastService.LoadBalancingRecommendation recommendation = demandForecastService
                                .getLoadBalancingRecommendation(stations, targetTime);

                return ResponseEntity.ok(recommendation);
        }

        // ==================== Scheduled Reports (Placeholder) ====================

        @PostMapping("/schedule")
        @Operation(summary = "Schedule automated report", description = "Schedule recurring report generation")
        public ResponseEntity<String> scheduleReport(
                        @Valid @RequestBody ScheduleReportRequest request) {

                logger.info("Report scheduling requested: {}", request);

                // TODO: Implement scheduled report functionality
                // This would typically involve:
                // 1. Store schedule configuration in database
                // 2. Use Spring @Scheduled or Quartz for execution
                // 3. Generate and email/store reports automatically

                return ResponseEntity.ok("Scheduled report feature coming soon");
        }

        // DTO for scheduling
        public record ScheduleReportRequest(
                        String reportType,
                        String frequency,
                        String email) {
        }
}
