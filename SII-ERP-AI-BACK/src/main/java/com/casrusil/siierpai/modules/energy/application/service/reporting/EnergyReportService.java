package com.casrusil.siierpai.modules.energy.application.service.reporting;

import com.casrusil.siierpai.modules.energy.domain.model.reporting.EnergyReport;
import com.casrusil.siierpai.modules.energy.domain.model.reporting.ReportId;
import com.casrusil.siierpai.modules.energy.domain.model.reporting.ReportPeriod;
import com.casrusil.siierpai.modules.energy.domain.model.reporting.ReportType;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating energy consumption reports.
 * 
 * Provides daily, weekly, monthly, and custom reports with comprehensive
 * energy usage analytics.
 */
@Service
@Transactional(readOnly = true)
public class EnergyReportService {

    private static final Logger logger = LoggerFactory.getLogger(EnergyReportService.class);

    private final ChargingSessionRepository sessionRepository;

    public EnergyReportService(ChargingSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Generates a daily energy report.
     */
    public EnergyReport generateDailyReport(LocalDate date) {
        logger.info("Generating daily report for {}", date);

        ReportPeriod period = ReportPeriod.daily(date);
        return generateReport(ReportType.DAILY, period);
    }

    /**
     * Generates a weekly energy report.
     */
    public EnergyReport generateWeeklyReport(LocalDate endDate) {
        logger.info("Generating weekly report ending {}", endDate);

        ReportPeriod period = ReportPeriod.weekly(endDate);
        return generateReport(ReportType.WEEKLY, period);
    }

    /**
     * Generates a monthly energy report.
     */
    public EnergyReport generateMonthlyReport(int year, int month) {
        logger.info("Generating monthly report for {}-{}", year, month);

        ReportPeriod period = ReportPeriod.monthly(year, month);
        return generateReport(ReportType.MONTHLY, period);
    }

    /**
     * Generates a yearly energy report.
     */
    public EnergyReport generateYearlyReport(int year) {
        logger.info("Generating yearly report for {}", year);

        ReportPeriod period = ReportPeriod.yearly(year);
        return generateReport(ReportType.YEARLY, period);
    }

    /**
     * Generates a custom period energy report.
     */
    public EnergyReport generateCustomReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating custom report from {} to {}", startDate, endDate);

        ReportPeriod period = ReportPeriod.of(startDate, endDate);
        return generateReport(ReportType.CUSTOM, period);
    }

    /**
     * Core report generation logic.
     */
    private EnergyReport generateReport(ReportType type, ReportPeriod period) {
        LocalDateTime startDateTime = period.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = period.getEndDate().atTime(23, 59, 59);

        // Fetch all completed sessions in the period
        var sessions = sessionRepository.findCompletedBetween(startDateTime, endDateTime);

        logger.debug("Found {} sessions for report period", sessions.size());

        // Calculate totals
        BigDecimal totalEnergy = sessions.stream()
                .map(session -> session.getTotalEnergyConsumed())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // For revenue/cost, we'd need to integrate with accounting
        // For now, using simplified calculation
        BigDecimal pricePerKwh = new BigDecimal("150.0");
        BigDecimal costPerKwh = new BigDecimal("80.0");

        BigDecimal totalRevenue = totalEnergy.multiply(pricePerKwh);
        BigDecimal totalCost = totalEnergy.multiply(costPerKwh);

        int sessionCount = sessions.size();

        // Calculate average session duration
        Duration averageDuration = sessionCount > 0
                ? Duration.ofSeconds(
                        sessions.stream()
                                .mapToLong(session -> session.getDuration().getSeconds())
                                .sum() / sessionCount)
                : Duration.ZERO;

        // Calculate hourly usage
        List<EnergyReport.HourlyUsage> hourlyUsage = calculateHourlyUsage(sessions);

        // Calculate top stations
        List<EnergyReport.StationUsage> topStations = calculateTopStations(sessions, pricePerKwh);

        return EnergyReport.builder()
                .id(ReportId.generate())
                .type(type)
                .period(period)
                .totalEnergyConsumed(totalEnergy)
                .totalRevenue(totalRevenue)
                .totalCost(totalCost)
                .sessionCount(sessionCount)
                .averageSessionDuration(averageDuration)
                .peakUsageHours(hourlyUsage)
                .topStations(topStations)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Calculates hourly usage distribution.
     */
    private List<EnergyReport.HourlyUsage> calculateHourlyUsage(List<?> sessions) {
        // Simplified implementation - in production, would analyze actual session times
        List<EnergyReport.HourlyUsage> hourlyUsage = new ArrayList<>();

        // For now, return empty list
        // TODO: Implement actual hourly analysis

        return hourlyUsage;
    }

    /**
     * Calculates top stations by energy consumption.
     */
    private List<EnergyReport.StationUsage> calculateTopStations(List<?> sessions, BigDecimal pricePerKwh) {
        // Simplified implementation
        List<EnergyReport.StationUsage> topStations = new ArrayList<>();

        // TODO: Group by station and calculate totals

        return topStations;
    }

    /**
     * Exports report to CSV format.
     */
    public String exportToCSV(EnergyReport report) {
        logger.info("Exporting report {} to CSV", report.getId());

        StringBuilder csv = new StringBuilder();
        csv.append("Energy Report - ").append(report.getType()).append("\n");
        csv.append("Period,").append(report.getPeriod()).append("\n");
        csv.append("Generated,").append(report.getGeneratedAt()).append("\n\n");

        csv.append("Metric,Value\n");
        csv.append("Total Energy (kWh),").append(report.getTotalEnergyConsumed()).append("\n");
        csv.append("Total Revenue,").append(report.getTotalRevenue()).append("\n");
        csv.append("Total Cost,").append(report.getTotalCost()).append("\n");
        csv.append("Net Profit,").append(report.getNetProfit()).append("\n");
        csv.append("Profit Margin (%),").append(report.getProfitMargin()).append("\n");
        csv.append("Session Count,").append(report.getSessionCount()).append("\n");
        csv.append("Avg Energy/Session,").append(report.getAverageEnergyPerSession()).append("\n");
        csv.append("Avg Revenue/Session,").append(report.getAverageRevenuePerSession()).append("\n");

        return csv.toString();
    }
}
