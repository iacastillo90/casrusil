package com.casrusil.siierpai.modules.energy.application.service.reporting;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating revenue-focused reports and profit analysis.
 * 
 * Provides detailed revenue breakdowns by station, vehicle, user, and time
 * period.
 */
@Service
@Transactional(readOnly = true)
public class RevenueReportService {

        private static final Logger logger = LoggerFactory.getLogger(RevenueReportService.class);

        private final ChargingSessionRepository sessionRepository;

        @Value("${ocpp.pricing.price-per-kwh:150.0}")
        private BigDecimal pricePerKwh;

        @Value("${ocpp.pricing.cost-per-kwh:80.0}")
        private BigDecimal costPerKwh;

        public RevenueReportService(ChargingSessionRepository sessionRepository) {
                this.sessionRepository = sessionRepository;
        }

        /**
         * Gets daily revenue for a specific date.
         */
        public RevenueData getDailyRevenue(LocalDate date) {
                logger.info("Calculating daily revenue for {}", date);

                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(23, 59, 59);

                return calculateRevenue(start, end);
        }

        /**
         * Gets monthly revenue for a specific month.
         */
        public RevenueData getMonthlyRevenue(int year, int month) {
                logger.info("Calculating monthly revenue for {}-{}", year, month);

                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.plusMonths(1).minusDays(1);

                return calculateRevenue(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        }

        /**
         * Gets revenue breakdown by station.
         */
        public List<StationRevenue> getRevenueByStation(LocalDate startDate, LocalDate endDate) {
                logger.info("Calculating revenue by station from {} to {}", startDate, endDate);

                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);

                List<ChargingSession> sessions = sessionRepository.findCompletedBetween(start, end);

                // Group by station
                Map<StationId, List<ChargingSession>> sessionsByStation = sessions.stream()
                                .collect(Collectors.groupingBy(ChargingSession::getStationId));

                return sessionsByStation.entrySet().stream()
                                .map(entry -> {
                                        StationId stationId = entry.getKey();
                                        List<ChargingSession> stationSessions = entry.getValue();

                                        BigDecimal totalEnergy = stationSessions.stream()
                                                        .map(ChargingSession::getTotalEnergyConsumed)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal revenue = totalEnergy.multiply(pricePerKwh);
                                        BigDecimal cost = totalEnergy.multiply(costPerKwh);
                                        BigDecimal profit = revenue.subtract(cost);

                                        return new StationRevenue(
                                                        stationId.getValue().toString(),
                                                        "Station " + stationId.getValue().toString().substring(0, 8), // Simplified
                                                                                                                      // name
                                                        totalEnergy,
                                                        revenue,
                                                        cost,
                                                        profit,
                                                        stationSessions.size());
                                })
                                .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                                .collect(Collectors.toList());
        }

        /**
         * Gets revenue breakdown by vehicle.
         */
        public List<VehicleRevenue> getRevenueByVehicle(LocalDate startDate, LocalDate endDate) {
                logger.info("Calculating revenue by vehicle from {} to {}", startDate, endDate);

                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);

                List<ChargingSession> sessions = sessionRepository.findCompletedBetween(start, end);

                // Group by user (vehicle owner)
                Map<UUID, List<ChargingSession>> sessionsByUser = sessions.stream()
                                .collect(Collectors.groupingBy(ChargingSession::getUserId));

                return sessionsByUser.entrySet().stream()
                                .map(entry -> {
                                        UUID userId = entry.getKey();
                                        List<ChargingSession> userSessions = entry.getValue();

                                        BigDecimal totalEnergy = userSessions.stream()
                                                        .map(ChargingSession::getTotalEnergyConsumed)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal revenue = totalEnergy.multiply(pricePerKwh);

                                        return new VehicleRevenue(
                                                        userId.toString(),
                                                        "User " + userId.toString().substring(0, 8), // Simplified name
                                                        totalEnergy,
                                                        revenue,
                                                        userSessions.size());
                                })
                                .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                                .collect(Collectors.toList());
        }

        /**
         * Gets revenue breakdown by user.
         */
        public List<UserRevenue> getRevenueByUser(LocalDate startDate, LocalDate endDate) {
                logger.info("Calculating revenue by user from {} to {}", startDate, endDate);

                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);

                List<ChargingSession> sessions = sessionRepository.findCompletedBetween(start, end);

                Map<UUID, List<ChargingSession>> sessionsByUser = sessions.stream()
                                .collect(Collectors.groupingBy(ChargingSession::getUserId));

                return sessionsByUser.entrySet().stream()
                                .map(entry -> {
                                        UUID userId = entry.getKey();
                                        List<ChargingSession> userSessions = entry.getValue();

                                        BigDecimal totalEnergy = userSessions.stream()
                                                        .map(ChargingSession::getTotalEnergyConsumed)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal revenue = totalEnergy.multiply(pricePerKwh);
                                        BigDecimal cost = totalEnergy.multiply(costPerKwh);

                                        return new UserRevenue(
                                                        userId.toString(),
                                                        totalEnergy,
                                                        revenue,
                                                        cost,
                                                        userSessions.size());
                                })
                                .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                                .collect(Collectors.toList());
        }

        /**
         * Performs comprehensive profit analysis.
         */
        public ProfitAnalysis getProfitAnalysis(LocalDate startDate, LocalDate endDate) {
                logger.info("Performing profit analysis from {} to {}", startDate, endDate);

                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);

                RevenueData revenueData = calculateRevenue(start, end);
                List<StationRevenue> stationRevenues = getRevenueByStation(startDate, endDate);

                // Find most and least profitable stations
                StationRevenue mostProfitable = stationRevenues.stream()
                                .max(Comparator.comparing(StationRevenue::profit))
                                .orElse(null);

                StationRevenue leastProfitable = stationRevenues.stream()
                                .min(Comparator.comparing(StationRevenue::profit))
                                .orElse(null);

                // Calculate profit margin
                BigDecimal profitMargin = revenueData.totalRevenue().compareTo(BigDecimal.ZERO) > 0
                                ? revenueData.netProfit()
                                                .divide(revenueData.totalRevenue(), 4, java.math.RoundingMode.HALF_UP)
                                                .multiply(new BigDecimal("100"))
                                : BigDecimal.ZERO;

                return new ProfitAnalysis(
                                revenueData,
                                profitMargin,
                                mostProfitable,
                                leastProfitable,
                                stationRevenues.size());
        }

        /**
         * Core revenue calculation logic.
         */
        public RevenueData calculateRevenue(LocalDateTime start, LocalDateTime end) {
                List<ChargingSession> sessions = sessionRepository.findCompletedBetween(start, end);

                BigDecimal totalEnergy = sessions.stream()
                                .map(ChargingSession::getTotalEnergyConsumed)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalRevenue = totalEnergy.multiply(pricePerKwh);
                BigDecimal totalCost = totalEnergy.multiply(costPerKwh);
                BigDecimal netProfit = totalRevenue.subtract(totalCost);

                return new RevenueData(
                                totalEnergy,
                                totalRevenue,
                                totalCost,
                                netProfit,
                                sessions.size());
        }

        // DTOs
        public record RevenueData(
                        BigDecimal totalEnergy,
                        BigDecimal totalRevenue,
                        BigDecimal totalCost,
                        BigDecimal netProfit,
                        int sessionCount) {
        }

        public record StationRevenue(
                        String stationId,
                        String stationName,
                        BigDecimal energyConsumed,
                        BigDecimal revenue,
                        BigDecimal cost,
                        BigDecimal profit,
                        int sessionCount) {
        }

        public record VehicleRevenue(
                        String vehicleId,
                        String vehicleName,
                        BigDecimal energyConsumed,
                        BigDecimal revenue,
                        int sessionCount) {
        }

        public record UserRevenue(
                        String userId,
                        BigDecimal energyConsumed,
                        BigDecimal revenue,
                        BigDecimal cost,
                        int sessionCount) {
        }

        public record ProfitAnalysis(
                        RevenueData overall,
                        BigDecimal profitMargin,
                        StationRevenue mostProfitableStation,
                        StationRevenue leastProfitableStation,
                        int totalStations) {
        }
}
