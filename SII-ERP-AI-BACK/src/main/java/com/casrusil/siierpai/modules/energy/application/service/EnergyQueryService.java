package com.casrusil.siierpai.modules.energy.application.service;

import com.casrusil.siierpai.modules.energy.domain.model.Charger;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingSessionStatus;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingStation;
import com.casrusil.siierpai.modules.energy.domain.model.StationStatus;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.Alert;
import com.casrusil.siierpai.modules.energy.domain.port.out.AlertRepository;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingStationRepository;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EnergyQueryService {

    private static final Logger logger = LoggerFactory.getLogger(EnergyQueryService.class);

    private final ChargingStationRepository stationRepository;
    private final ChargingSessionRepository sessionRepository;
    private final AlertRepository alertRepository;

    public EnergyQueryService(
            ChargingStationRepository stationRepository,
            ChargingSessionRepository sessionRepository,
            AlertRepository alertRepository) {
        this.stationRepository = stationRepository;
        this.sessionRepository = sessionRepository;
        this.alertRepository = alertRepository;
    }

    public List<ChargingStation> getStationsByCompany() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        logger.info("Fetching stations for company: {}", companyId);
        return stationRepository.findByCompanyId(companyId);
    }

    public int getTotalStationsByCompany() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        return (int) stationRepository.findByCompanyId(companyId).stream()
                .filter(s -> s.getStatus() != StationStatus.UNDER_CONSTRUCTION && 
                            s.getStatus() != StationStatus.DECOMMISSIONED)
                .count();
    }

    public int getActiveStationsByCompany() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        return (int) stationRepository.findByCompanyId(companyId).stream()
                .filter(s -> s.getStatus() == StationStatus.OPERATIONAL && s.isActive())
                .count();
    }

    public double getTodayConsumptionByCompany() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        List<ChargingStation> stations = stationRepository.findByCompanyId(companyId);
        if (stations.isEmpty()) return 0.0;

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        return sessionRepository.findCompletedBetween(startOfDay, endOfDay).stream()
                .mapToDouble(s -> s.getTotalEnergyConsumed().doubleValue())
                .sum();
    }

    public int getActiveSessionsByCompany() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        List<ChargingStation> stations = stationRepository.findByCompanyId(companyId);
        if (stations.isEmpty()) return 0;

        return (int) stations.stream()
                .mapToInt(station -> (int) sessionRepository.findActiveSessionsByStation(station.getStationId()).size())
                .sum();
    }

    public List<ChargingSession> getRecentSessionsByCompany(int limit) {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        List<ChargingStation> stations = stationRepository.findByCompanyId(companyId);
        if (stations.isEmpty()) return List.of();

        List<ChargingSession> allSessions = sessionRepository.findCompletedBetween(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );

        return allSessions.stream()
                .limit(Math.min(limit, 50))
                .toList();
    }

    public List<Alert> getAlertsByCompany() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        List<ChargingStation> stations = stationRepository.findByCompanyId(companyId);

        return alertRepository.findAll().stream()
                .filter(a -> {
                    if (a.getStationId() == null) return false;
                    return stations.stream()
                            .anyMatch(s -> s.getStationId().getValue().equals(a.getStationId()));
                })
                .toList();
    }
}