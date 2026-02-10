package com.casrusil.siierpai.modules.energy.application.service.analytics;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for demand forecasting and capacity planning.
 * Uses historical data to predict future charging demand.
 */
@Service
@Transactional(readOnly = true)
public class DemandForecastService {

    private static final Logger logger = LoggerFactory.getLogger(DemandForecastService.class);

    private final ChargingSessionRepository sessionRepository;

    public DemandForecastService(ChargingSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Predicts future demand for a specific date and hour.
     */
    public DemandForecast predictDemand(
            StationId stationId,
            LocalDate targetDate,
            int targetHour) {

        logger.info("Predicting demand for station {} on {} at hour {}",
                stationId, targetDate, targetHour);

        // Load historical data (last 30 days)
        LocalDate startDate = targetDate.minusDays(30);
        List<ChargingSession> historicalSessions = loadHistoricalSessions(
                stationId, startDate, targetDate);

        if (historicalSessions.isEmpty()) {
            logger.warn("No historical data available for forecasting");
            return createEmptyForecast(targetDate, targetHour);
        }

        // Calculate average demand for same hour and day of week
        DayOfWeek targetDayOfWeek = targetDate.getDayOfWeek();
        double predictedSessions = predictSessionCount(
                historicalSessions, targetDayOfWeek, targetHour);

        double predictedEnergyKwh = predictEnergyConsumption(
                historicalSessions, targetDayOfWeek, targetHour);

        double confidenceLevel = calculateConfidence(historicalSessions, targetDayOfWeek, targetHour);

        logger.info("Forecast: {} sessions, {} kWh, {}% confidence",
                predictedSessions, predictedEnergyKwh, confidenceLevel * 100);

        return new DemandForecast(
                stationId,
                targetDate,
                targetHour,
                predictedSessions,
                predictedEnergyKwh,
                confidenceLevel);
    }

    /**
     * Generates capacity planning recommendations.
     */
    public CapacityPlanningReport generateCapacityPlan(
            StationId stationId,
            LocalDate startDate,
            LocalDate endDate) {

        logger.info("Generating capacity plan for station {} from {} to {}",
                stationId, startDate, endDate);

        List<ChargingSession> sessions = loadHistoricalSessions(stationId, startDate, endDate);

        if (sessions.isEmpty()) {
            return createEmptyCapacityPlan(stationId);
        }

        // Calculate peak demand
        PeakDemandAnalysis peakAnalysis = analyzePeakDemand(sessions);

        // Calculate utilization
        double utilizationRate = calculateUtilizationRate(sessions, startDate, endDate);

        // Generate recommendations
        List<String> recommendations = generateRecommendations(
                peakAnalysis, utilizationRate);

        return new CapacityPlanningReport(
                stationId,
                peakAnalysis,
                utilizationRate,
                recommendations,
                LocalDateTime.now());
    }

    /**
     * Provides load balancing recommendations.
     */
    public LoadBalancingRecommendation getLoadBalancingRecommendation(
            List<StationId> stationIds,
            LocalDateTime targetTime) {

        logger.info("Generating load balancing recommendation for {} stations at {}",
                stationIds.size(), targetTime);

        Map<StationId, Double> stationLoads = new HashMap<>();

        for (StationId stationId : stationIds) {
            DemandForecast forecast = predictDemand(
                    stationId,
                    targetTime.toLocalDate(),
                    targetTime.getHour());
            stationLoads.put(stationId, forecast.predictedEnergyKwh());
        }

        // Find least loaded station
        StationId recommendedStation = stationLoads.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(stationIds.get(0));

        double averageLoad = stationLoads.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return new LoadBalancingRecommendation(
                recommendedStation,
                stationLoads,
                averageLoad,
                targetTime);
    }

    // Private helper methods

    private List<ChargingSession> loadHistoricalSessions(
            StationId stationId,
            LocalDate startDate,
            LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return sessionRepository.findCompletedBetween(startDateTime, endDateTime)
                .stream()
                .filter(s -> s.getStationId().equals(stationId))
                .toList();
    }

    private double predictSessionCount(
            List<ChargingSession> sessions,
            DayOfWeek dayOfWeek,
            int hour) {

        List<ChargingSession> relevantSessions = sessions.stream()
                .filter(s -> s.getStartedAt() != null)
                .filter(s -> s.getStartedAt().getDayOfWeek() == dayOfWeek)
                .filter(s -> s.getStartedAt().getHour() == hour)
                .toList();

        if (relevantSessions.isEmpty()) {
            return 0.0;
        }

        // Group by date and count sessions per day
        Map<LocalDate, Long> sessionsByDate = relevantSessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStartedAt().toLocalDate(),
                        Collectors.counting()));

        // Calculate average
        return sessionsByDate.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    private double predictEnergyConsumption(
            List<ChargingSession> sessions,
            DayOfWeek dayOfWeek,
            int hour) {

        List<BigDecimal> energyValues = sessions.stream()
                .filter(s -> s.getStartedAt() != null)
                .filter(s -> s.getStartedAt().getDayOfWeek() == dayOfWeek)
                .filter(s -> s.getStartedAt().getHour() == hour)
                .filter(s -> s.getTotalEnergyConsumed() != null)
                .map(ChargingSession::getTotalEnergyConsumed)
                .toList();

        if (energyValues.isEmpty()) {
            return 0.0;
        }

        BigDecimal total = energyValues.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(
                BigDecimal.valueOf(energyValues.size()),
                2,
                RoundingMode.HALF_UP).doubleValue();
    }

    private double calculateConfidence(
            List<ChargingSession> sessions,
            DayOfWeek dayOfWeek,
            int hour) {

        long relevantCount = sessions.stream()
                .filter(s -> s.getStartedAt() != null)
                .filter(s -> s.getStartedAt().getDayOfWeek() == dayOfWeek)
                .filter(s -> s.getStartedAt().getHour() == hour)
                .count();

        // Confidence based on sample size
        if (relevantCount >= 20)
            return 0.95;
        if (relevantCount >= 10)
            return 0.80;
        if (relevantCount >= 5)
            return 0.60;
        if (relevantCount >= 2)
            return 0.40;
        return 0.20;
    }

    private PeakDemandAnalysis analyzePeakDemand(List<ChargingSession> sessions) {
        // Find hour with highest energy consumption
        Map<Integer, Double> energyByHour = new HashMap<>();

        for (ChargingSession session : sessions) {
            if (session.getStartedAt() != null && session.getTotalEnergyConsumed() != null) {
                int hour = session.getStartedAt().getHour();
                double energy = session.getTotalEnergyConsumed().doubleValue();
                energyByHour.merge(hour, energy, Double::sum);
            }
        }

        int peakHour = energyByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(12);

        double peakDemandKw = energyByHour.getOrDefault(peakHour, 0.0);

        return new PeakDemandAnalysis(peakHour, peakDemandKw);
    }

    private double calculateUtilizationRate(
            List<ChargingSession> sessions,
            LocalDate startDate,
            LocalDate endDate) {

        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long totalHoursAvailable = periodDays * 24;

        long totalChargingHours = sessions.stream()
                .filter(s -> s.getStartedAt() != null && s.getEndedAt() != null)
                .mapToLong(s -> java.time.Duration.between(
                        s.getStartedAt(), s.getEndedAt()).toHours())
                .sum();

        if (totalHoursAvailable == 0) {
            return 0.0;
        }

        return (totalChargingHours * 100.0) / totalHoursAvailable;
    }

    private List<String> generateRecommendations(
            PeakDemandAnalysis peakAnalysis,
            double utilizationRate) {

        List<String> recommendations = new ArrayList<>();

        if (utilizationRate > 80) {
            recommendations.add("HIGH_UTILIZATION: Consider adding more charging capacity");
            recommendations.add("LOAD_BALANCING: Implement dynamic pricing to shift demand");
        } else if (utilizationRate > 60) {
            recommendations.add("MODERATE_UTILIZATION: Monitor growth trends");
        } else {
            recommendations.add("LOW_UTILIZATION: Capacity is sufficient");
        }

        if (peakAnalysis.peakHour() >= 17 && peakAnalysis.peakHour() <= 20) {
            recommendations.add("PEAK_PRICING: Consider higher rates during evening peak (17:00-20:00)");
        }

        if (peakAnalysis.peakDemandKw() > 100) {
            recommendations.add("GRID_CAPACITY: Verify grid connection can handle peak demand");
        }

        return recommendations;
    }

    private DemandForecast createEmptyForecast(LocalDate date, int hour) {
        return new DemandForecast(
                null,
                date,
                hour,
                0.0,
                0.0,
                0.0);
    }

    private CapacityPlanningReport createEmptyCapacityPlan(StationId stationId) {
        return new CapacityPlanningReport(
                stationId,
                new PeakDemandAnalysis(12, 0.0),
                0.0,
                List.of("INSUFFICIENT_DATA: Not enough historical data for analysis"),
                LocalDateTime.now());
    }

    // DTOs

    public record DemandForecast(
            StationId stationId,
            LocalDate targetDate,
            int targetHour,
            double predictedSessions,
            double predictedEnergyKwh,
            double confidenceLevel) {
    }

    public record CapacityPlanningReport(
            StationId stationId,
            PeakDemandAnalysis peakDemand,
            double utilizationRate,
            List<String> recommendations,
            LocalDateTime generatedAt) {
    }

    public record PeakDemandAnalysis(
            int peakHour,
            double peakDemandKw) {
    }

    public record LoadBalancingRecommendation(
            StationId recommendedStation,
            Map<StationId, Double> stationLoads,
            double averageLoad,
            LocalDateTime targetTime) {
    }
}
