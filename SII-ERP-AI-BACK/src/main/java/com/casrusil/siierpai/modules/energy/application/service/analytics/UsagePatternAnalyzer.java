package com.casrusil.siierpai.modules.energy.application.service.analytics;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.reporting.UsagePattern;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for analyzing usage patterns and trends.
 */
@Service
@Transactional(readOnly = true)
public class UsagePatternAnalyzer {

        private static final Logger logger = LoggerFactory.getLogger(UsagePatternAnalyzer.class);

        private final ChargingSessionRepository sessionRepository;

        public UsagePatternAnalyzer(ChargingSessionRepository sessionRepository) {
                this.sessionRepository = sessionRepository;
        }

        /**
         * Analyzes usage patterns for a station over a date range.
         */
        public UsagePattern analyzePatterns(
                        StationId stationId,
                        LocalDate startDate,
                        LocalDate endDate) {

                logger.info("Analyzing usage patterns for station {} from {} to {}",
                                stationId, startDate, endDate);

                List<ChargingSession> sessions = loadSessions(stationId, startDate, endDate);

                if (sessions.isEmpty()) {
                        logger.warn("No sessions found for analysis");
                        return createEmptyPattern();
                }

                List<Integer> peakHours = detectPeakHours(sessions);
                List<Integer> offPeakHours = detectOffPeakHours(sessions, peakHours);
                List<DayOfWeek> busiestDays = findBusiestDays(sessions);
                Duration avgDuration = calculateAverageDuration(sessions);
                Double utilizationRate = calculateUtilization(sessions, startDate, endDate);
                UsagePattern.PeakDemandInfo peakDemand = findPeakDemand(sessions);

                logger.info("Analysis complete: {} peak hours, {} busiest days",
                                peakHours.size(), busiestDays.size());

                return new UsagePattern(
                                peakHours,
                                offPeakHours,
                                busiestDays,
                                avgDuration,
                                utilizationRate,
                                peakDemand);
        }

        /**
         * Loads charging sessions for analysis.
         */
        private List<ChargingSession> loadSessions(
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

        /**
         * Detects peak usage hours (top 3 hours with most sessions).
         */
        private List<Integer> detectPeakHours(List<ChargingSession> sessions) {
                Map<Integer, Long> sessionsByHour = sessions.stream()
                                .filter(s -> s.getStartedAt() != null)
                                .collect(Collectors.groupingBy(
                                                s -> s.getStartedAt().getHour(),
                                                Collectors.counting()));

                return sessionsByHour.entrySet().stream()
                                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                                .limit(3)
                                .map(Map.Entry::getKey)
                                .toList();
        }

        /**
         * Detects off-peak hours (hours with lowest usage).
         */
        private List<Integer> detectOffPeakHours(
                        List<ChargingSession> sessions,
                        List<Integer> peakHours) {

                Map<Integer, Long> sessionsByHour = sessions.stream()
                                .filter(s -> s.getStartedAt() != null)
                                .collect(Collectors.groupingBy(
                                                s -> s.getStartedAt().getHour(),
                                                Collectors.counting()));

                return sessionsByHour.entrySet().stream()
                                .filter(e -> !peakHours.contains(e.getKey()))
                                .sorted(Map.Entry.comparingByValue())
                                .limit(3)
                                .map(Map.Entry::getKey)
                                .toList();
        }

        /**
         * Finds the busiest days of the week (top 3).
         */
        private List<DayOfWeek> findBusiestDays(List<ChargingSession> sessions) {
                Map<DayOfWeek, Long> sessionsByDay = sessions.stream()
                                .filter(s -> s.getStartedAt() != null)
                                .collect(Collectors.groupingBy(
                                                s -> s.getStartedAt().getDayOfWeek(),
                                                Collectors.counting()));

                return sessionsByDay.entrySet().stream()
                                .sorted(Map.Entry.<DayOfWeek, Long>comparingByValue().reversed())
                                .limit(3)
                                .map(Map.Entry::getKey)
                                .toList();
        }

        /**
         * Calculates average session duration.
         */
        private Duration calculateAverageDuration(List<ChargingSession> sessions) {
                List<Duration> durations = sessions.stream()
                                .filter(s -> s.getStartedAt() != null && s.getEndedAt() != null)
                                .map(s -> Duration.between(s.getStartedAt(), s.getEndedAt()))
                                .toList();

                if (durations.isEmpty()) {
                        return Duration.ZERO;
                }

                long totalMinutes = durations.stream()
                                .mapToLong(Duration::toMinutes)
                                .sum();

                return Duration.ofMinutes(totalMinutes / durations.size());
        }

        /**
         * Calculates station utilization rate as percentage.
         */
        private Double calculateUtilization(
                        List<ChargingSession> sessions,
                        LocalDate startDate,
                        LocalDate endDate) {

                long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
                long totalHoursAvailable = periodDays * 24;

                long totalChargingHours = sessions.stream()
                                .filter(s -> s.getStartedAt() != null && s.getEndedAt() != null)
                                .mapToLong(s -> Duration.between(s.getStartedAt(), s.getEndedAt()).toHours())
                                .sum();

                if (totalHoursAvailable == 0) {
                        return 0.0;
                }

                return (totalChargingHours * 100.0) / totalHoursAvailable;
        }

        /**
         * Finds peak demand information.
         */
        private UsagePattern.PeakDemandInfo findPeakDemand(List<ChargingSession> sessions) {
                // Find the session with highest power demand
                var peakSession = sessions.stream()
                                .filter(s -> s.getTotalEnergyConsumed() != null && s.getStartedAt() != null)
                                .max((s1, s2) -> {
                                        // Simple heuristic: energy / duration = average power
                                        double power1 = calculateAveragePower(s1);
                                        double power2 = calculateAveragePower(s2);
                                        return Double.compare(power1, power2);
                                });

                if (peakSession.isEmpty()) {
                        return new UsagePattern.PeakDemandInfo(
                                        LocalTime.NOON,
                                        0,
                                        DayOfWeek.MONDAY);
                }

                ChargingSession peak = peakSession.get();
                int peakDemandKw = (int) calculateAveragePower(peak);

                return new UsagePattern.PeakDemandInfo(
                                peak.getStartedAt().toLocalTime(),
                                peakDemandKw,
                                peak.getStartedAt().getDayOfWeek());
        }

        /**
         * Calculates average power for a session in kW.
         */
        private double calculateAveragePower(ChargingSession session) {
                if (session.getTotalEnergyConsumed() == null ||
                                session.getStartedAt() == null ||
                                session.getEndedAt() == null) {
                        return 0.0;
                }

                Duration duration = Duration.between(session.getStartedAt(), session.getEndedAt());
                double hours = duration.toMinutes() / 60.0;

                if (hours == 0) {
                        return 0.0;
                }

                return session.getTotalEnergyConsumed().doubleValue() / hours;
        }

        /**
         * Creates an empty usage pattern for cases with no data.
         */
        private UsagePattern createEmptyPattern() {
                return new UsagePattern(
                                List.of(),
                                List.of(),
                                List.of(),
                                Duration.ZERO,
                                0.0,
                                new UsagePattern.PeakDemandInfo(LocalTime.NOON, 0, DayOfWeek.MONDAY));
        }
}
