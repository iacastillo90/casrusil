package com.casrusil.siierpai.modules.energy.application.service.monitoring;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.DashboardMetrics;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.MetricType;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingStationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service for collecting and aggregating dashboard metrics.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

        private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

        private final ChargingSessionRepository sessionRepository;
        private final ChargingStationRepository stationRepository;

        public DashboardService(
                        ChargingSessionRepository sessionRepository,
                        ChargingStationRepository stationRepository) {
                this.sessionRepository = sessionRepository;
                this.stationRepository = stationRepository;
        }

        /**
         * Gets current dashboard metrics snapshot.
         */
        public DashboardMetrics getCurrentMetrics() {
                logger.info("Collecting current dashboard metrics");

                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime now = LocalDateTime.now();

                // Get all sessions today
                var sessionsToday = sessionRepository.findCompletedBetween(startOfDay, now);

                // Calculate active sessions (sessions without end time)
                var activeSessions = sessionsToday.stream()
                                .filter(s -> s.getEndedAt() == null)
                                .toList();

                // Get all stations count
                int totalStations = (int) stationRepository.count();
                int activeStations = (int) stationRepository.countByStatus(com.casrusil.siierpai.modules.energy.domain.model.StationStatus.OPERATIONAL);
                int offlineStations = totalStations - activeStations;

                // Calculate session metrics
                int totalSessionsToday = sessionsToday.size();
                int activeSessionCount = activeSessions.size();
                int completedSessionsToday = sessionsToday.size() - activeSessionCount;

                // Calculate energy metrics
                BigDecimal totalEnergyToday = sessionsToday.stream()
                                .map(ChargingSession::getTotalEnergyConsumed)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Simplified current power (would need real-time data)
                BigDecimal currentPower = BigDecimal.ZERO;

                BigDecimal avgEnergyPerSession = completedSessionsToday > 0
                                ? totalEnergyToday.divide(BigDecimal.valueOf(completedSessionsToday), 2,
                                                RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Calculate revenue metrics (simplified - using fixed rate)
                BigDecimal pricePerKwh = new BigDecimal("150.0");
                BigDecimal totalRevenueToday = totalEnergyToday.multiply(pricePerKwh);
                BigDecimal avgRevenuePerSession = completedSessionsToday > 0
                                ? totalRevenueToday.divide(BigDecimal.valueOf(completedSessionsToday), 2,
                                                RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Calculate station metrics
                BigDecimal utilizationRate = totalStations > 0
                                ? BigDecimal.valueOf(activeSessionCount)
                                                .divide(BigDecimal.valueOf(totalStations), 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO;

                // Calculate performance metrics
                int avgDurationMinutes = completedSessionsToday > 0
                                ? (int) sessionsToday.stream()
                                                .filter(s -> s.getDuration() != null)
                                                .mapToLong(s -> s.getDuration().toMinutes())
                                                .average()
                                                .orElse(0)
                                : 0;

                // Build metrics
                DashboardMetrics metrics = DashboardMetrics.builder()
                                .id(com.casrusil.siierpai.modules.energy.domain.model.monitoring.MetricId.generate())
                                .timestamp(now)
                                .activeSessions(activeSessionCount)
                                .totalSessionsToday(totalSessionsToday)
                                .completedSessionsToday(completedSessionsToday)
                                .totalEnergyToday(totalEnergyToday)
                                .currentPowerConsumption(currentPower)
                                .averageEnergyPerSession(avgEnergyPerSession)
                                .totalRevenueToday(totalRevenueToday)
                                .averageRevenuePerSession(avgRevenuePerSession)
                                .activeStations(activeStations)
                                .offlineStations(offlineStations)
                                .totalStations(totalStations)
                                .stationUtilizationRate(utilizationRate)
                                .averageSessionDurationMinutes(avgDurationMinutes)
                                .peakPowerToday(currentPower) // Simplified - would track max
                                .build();

                logger.info("Dashboard metrics collected: {} active sessions, {} kWh today",
                                activeSessionCount, totalEnergyToday);

                return metrics;
        }

        /**
         * Gets metrics for a specific metric type.
         */
        public BigDecimal getMetricValue(MetricType metricType) {
                DashboardMetrics metrics = getCurrentMetrics();

                return switch (metricType) {
                        case ACTIVE_SESSIONS -> BigDecimal.valueOf(metrics.getActiveSessions());
                        case TOTAL_SESSIONS_TODAY -> BigDecimal.valueOf(metrics.getTotalSessionsToday());
                        case COMPLETED_SESSIONS_TODAY -> BigDecimal.valueOf(metrics.getCompletedSessionsToday());
                        case TOTAL_ENERGY_TODAY -> metrics.getTotalEnergyToday();
                        case CURRENT_POWER_CONSUMPTION -> metrics.getCurrentPowerConsumption();
                        case AVERAGE_ENERGY_PER_SESSION -> metrics.getAverageEnergyPerSession();
                        case TOTAL_REVENUE_TODAY -> metrics.getTotalRevenueToday();
                        case AVERAGE_REVENUE_PER_SESSION -> metrics.getAverageRevenuePerSession();
                        case ACTIVE_STATIONS -> BigDecimal.valueOf(metrics.getActiveStations());
                        case OFFLINE_STATIONS -> BigDecimal.valueOf(metrics.getOfflineStations());
                        case TOTAL_STATIONS -> BigDecimal.valueOf(metrics.getTotalStations());
                        case STATION_UTILIZATION_RATE -> metrics.getStationUtilizationRate();
                        case AVERAGE_SESSION_DURATION -> BigDecimal.valueOf(metrics.getAverageSessionDurationMinutes());
                        case PEAK_POWER_TODAY -> metrics.getPeakPowerToday();
                        default -> BigDecimal.ZERO;
                };
        }
}
