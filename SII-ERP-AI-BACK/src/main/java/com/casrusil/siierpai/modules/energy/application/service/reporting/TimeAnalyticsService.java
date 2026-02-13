package com.casrusil.siierpai.modules.energy.application.service.reporting;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for time-based analytics of energy consumption.
 * 
 * Provides insights into usage patterns, peak hours, and demand forecasting.
 */
@Service
@Transactional(readOnly = true)
public class TimeAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(TimeAnalyticsService.class);

    private final ChargingSessionRepository sessionRepository;

    public TimeAnalyticsService(ChargingSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Gets energy usage distribution by hour of day.
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return Map of hour (0-23) to total energy consumed
     */
    public Map<Integer, BigDecimal> getUsageByHour(LocalDate startDate, LocalDate endDate) {
        logger.info("Analyzing usage by hour from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<ChargingSession> sessions = sessionRepository.findCompletedBetween(start, end);

        Map<Integer, BigDecimal> usageByHour = new HashMap<>();

        // Initialize all hours
        for (int hour = 0; hour < 24; hour++) {
            usageByHour.put(hour, BigDecimal.ZERO);
        }

        // Aggregate energy by hour
        for (ChargingSession session : sessions) {
            int hour = session.getStartedAt().getHour();
            BigDecimal energy = session.getTotalEnergyConsumed();
            usageByHour.merge(hour, energy, BigDecimal::add);
        }

        return usageByHour;
    }

    /**
     * Gets energy usage distribution by day of week.
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return Map of day of week to total energy consumed
     */
    public Map<DayOfWeek, BigDecimal> getUsageByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        logger.info("Analyzing usage by day of week from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<ChargingSession> sessions = sessionRepository.findCompletedBetween(start, end);

        Map<DayOfWeek, BigDecimal> usageByDay = new EnumMap<>(DayOfWeek.class);

        // Initialize all days
        for (DayOfWeek day : DayOfWeek.values()) {
            usageByDay.put(day, BigDecimal.ZERO);
        }

        // Aggregate energy by day
        for (ChargingSession session : sessions) {
            DayOfWeek day = session.getStartedAt().getDayOfWeek();
            BigDecimal energy = session.getTotalEnergyConsumed();
            usageByDay.merge(day, energy, BigDecimal::add);
        }

        return usageByDay;
    }

    /**
     * Identifies peak usage hours.
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @param topN      Number of peak hours to return
     * @return List of peak hours with usage data
     */
    public List<PeakHour> getPeakHours(LocalDate startDate, LocalDate endDate, int topN) {
        logger.info("Identifying top {} peak hours", topN);

        Map<Integer, BigDecimal> usageByHour = getUsageByHour(startDate, endDate);

        return usageByHour.entrySet().stream()
                .map(entry -> new PeakHour(
                        entry.getKey(),
                        entry.getValue(),
                        calculateSessionCount(startDate, endDate, entry.getKey())))
                .sorted((a, b) -> b.energyConsumed().compareTo(a.energyConsumed()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Identifies off-peak hours (lowest usage).
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @param topN      Number of off-peak hours to return
     * @return List of off-peak hours with usage data
     */
    public List<PeakHour> getOffPeakHours(LocalDate startDate, LocalDate endDate, int topN) {
        logger.info("Identifying top {} off-peak hours", topN);

        Map<Integer, BigDecimal> usageByHour = getUsageByHour(startDate, endDate);

        return usageByHour.entrySet().stream()
                .map(entry -> new PeakHour(
                        entry.getKey(),
                        entry.getValue(),
                        calculateSessionCount(startDate, endDate, entry.getKey())))
                .sorted(Comparator.comparing(PeakHour::energyConsumed))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Predicts energy demand for future periods based on historical data.
     * 
     * @param historicalDays Number of historical days to analyze
     * @param forecastDays   Number of days to forecast
     * @return Demand forecast
     */
    public DemandForecast predictDemand(int historicalDays, int forecastDays) {
        logger.info("Predicting demand for {} days based on {} historical days",
                forecastDays, historicalDays);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(historicalDays);

        // Calculate historical average
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<ChargingSession> sessions = sessionRepository.findCompletedBetween(start, end);

        BigDecimal totalEnergy = sessions.stream()
                .map(ChargingSession::getTotalEnergyConsumed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageDailyEnergy = totalEnergy.divide(
                new BigDecimal(historicalDays),
                2,
                java.math.RoundingMode.HALF_UP);

        // Simple linear forecast (can be enhanced with ML models)
        BigDecimal forecastedEnergy = averageDailyEnergy.multiply(new BigDecimal(forecastDays));

        // Calculate growth trend
        BigDecimal growthRate = calculateGrowthRate(sessions, historicalDays);

        return new DemandForecast(
                averageDailyEnergy,
                forecastedEnergy,
                growthRate,
                LocalDate.now().plusDays(forecastDays));
    }

    /**
     * Analyzes usage patterns to identify trends.
     */
    public UsagePattern analyzeUsagePattern(LocalDate startDate, LocalDate endDate) {
        logger.info("Analyzing usage patterns from {} to {}", startDate, endDate);

        Map<Integer, BigDecimal> hourlyUsage = getUsageByHour(startDate, endDate);
        Map<DayOfWeek, BigDecimal> dailyUsage = getUsageByDayOfWeek(startDate, endDate);

        // Find peak hour
        int peakHour = hourlyUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        // Find peak day
        DayOfWeek peakDay = dailyUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);

        // Calculate consistency (standard deviation)
        double consistency = calculateConsistency(hourlyUsage.values());

        return new UsagePattern(
                peakHour,
                peakDay,
                hourlyUsage,
                dailyUsage,
                consistency);
    }

    /**
     * Calculates session count for a specific hour.
     */
    private int calculateSessionCount(LocalDate startDate, LocalDate endDate, int hour) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<ChargingSession> sessions = sessionRepository.findCompletedBetween(start, end);

        return (int) sessions.stream()
                .filter(session -> session.getStartedAt().getHour() == hour)
                .count();
    }

    /**
     * Calculates growth rate from historical data.
     */
    private BigDecimal calculateGrowthRate(List<ChargingSession> sessions, int days) {
        if (days < 2 || sessions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Split into first half and second half
        int midPoint = days / 2;
        LocalDate now = LocalDate.now();
        LocalDate midDate = now.minusDays(midPoint);

        BigDecimal firstHalfEnergy = sessions.stream()
                .filter(s -> s.getStartedAt().toLocalDate().isBefore(midDate))
                .map(ChargingSession::getTotalEnergyConsumed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal secondHalfEnergy = sessions.stream()
                .filter(s -> !s.getStartedAt().toLocalDate().isBefore(midDate))
                .map(ChargingSession::getTotalEnergyConsumed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (firstHalfEnergy.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return secondHalfEnergy.subtract(firstHalfEnergy)
                .divide(firstHalfEnergy, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Calculates consistency score (lower is more consistent).
     */
    private double calculateConsistency(Collection<BigDecimal> values) {
        if (values.isEmpty()) {
            return 0.0;
        }

        double mean = values.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v.doubleValue() - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    // DTOs
    public record PeakHour(
            int hour,
            BigDecimal energyConsumed,
            int sessionCount) {
    }

    public record DemandForecast(
            BigDecimal averageDailyEnergy,
            BigDecimal forecastedTotalEnergy,
            BigDecimal growthRate,
            LocalDate forecastDate) {
    }

    public record UsagePattern(
            int peakHour,
            DayOfWeek peakDay,
            Map<Integer, BigDecimal> hourlyDistribution,
            Map<DayOfWeek, BigDecimal> dailyDistribution,
            double consistencyScore) {
    }
}
