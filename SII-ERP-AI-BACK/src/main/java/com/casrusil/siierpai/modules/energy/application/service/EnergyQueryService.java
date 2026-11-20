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
        List<LocalDateTime> range = todayRange();
        return completedTodayByCompany(range.get(0), range.get(1)).stream()
                .mapToDouble(s -> s.getTotalEnergyConsumed().doubleValue())
                .sum();
    }

    /**
     * Potencia pico del día: la mayor potencia promedio de una sesión completada
     * hoy (energía / duración). Devuelve {@code null} si no hay sesiones con
     * duración medible, para no inventar un 0 que parezca un dato real.
     */
    public Double getPeakAveragePowerTodayByCompany() {
        List<LocalDateTime> range = todayRange();
        return completedTodayByCompany(range.get(0), range.get(1)).stream()
                .map(this::averagePowerKw)
                .filter(java.util.Objects::nonNull)
                .max(Double::compareTo)
                .map(value -> Math.round(value * 100.0) / 100.0)
                .orElse(null);
    }

    /** Potencia promedio de la sesión (kWh / horas), o null si no es medible. */
    private Double averagePowerKw(ChargingSession session) {
        if (session.getStartedAt() == null || session.getEndedAt() == null) {
            return null;
        }
        long minutes = java.time.Duration.between(session.getStartedAt(), session.getEndedAt()).toMinutes();
        if (minutes <= 0) {
            return null;
        }
        double hours = minutes / 60.0;
        return session.getTotalEnergyConsumed().doubleValue() / hours;
    }

    private List<LocalDateTime> todayRange() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return List.of(startOfDay, endOfDay);
    }

    /**
     * Sesiones completadas hoy de las estaciones de la empresa.
     * Antes se usaba {@code findCompletedBetween} sin filtrar por empresa
     * (fuga de datos entre tenants).
     */
    private List<ChargingSession> completedTodayByCompany(LocalDateTime start, LocalDateTime end) {
        List<ChargingStation> stations = stationsOfCurrentCompany();
        if (stations.isEmpty()) {
            return List.of();
        }
        List<UUID> stationIds = stations.stream()
                .map(station -> station.getStationId().getValue())
                .toList();
        return sessionRepository.findCompletedByStationIds(stationIds, start, end);
    }

    private List<ChargingStation> stationsOfCurrentCompany() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        return stationRepository.findByCompanyId(companyId);
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
        List<ChargingStation> stations = stationsOfCurrentCompany();
        if (stations.isEmpty()) return List.of();

        List<UUID> stationIds = stations.stream()
                .map(station -> station.getStationId().getValue())
                .toList();

        return sessionRepository.findRecentByStationIds(stationIds, Math.min(limit, 50));
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