package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.energy.application.service.EnergyQueryService;
import com.casrusil.siierpai.modules.energy.domain.model.Charger;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingStation;
import com.casrusil.siierpai.modules.energy.domain.model.Connector;
import com.casrusil.siierpai.modules.energy.domain.model.StationStatus;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.Alert;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto.ChargeSessionDto;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto.EnergyAlertDto;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto.EnergyMetricsDto;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto.EnergyStationDto;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/energy")
@Tag(name = "Energy Management", description = "Energy stations, metrics, alerts, and charging sessions")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS','TECHNICIAN')")
public class EnergyController {

    private static final Logger logger = LoggerFactory.getLogger(EnergyController.class);

    private final EnergyQueryService energyQueryService;

    public EnergyController(EnergyQueryService energyQueryService) {
        this.energyQueryService = energyQueryService;
    }

    @GetMapping("/stations")
    @Operation(summary = "Get all charging stations for the company")
    public ResponseEntity<List<EnergyStationDto>> getStations() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        logger.info("Fetching stations for company: {}", companyId);

        List<ChargingStation> stations = energyQueryService.getStationsByCompany();
        List<EnergyStationDto> stationsDto = stations.stream()
                .map(EnergyStationDto::from)
                .toList();

        return ResponseEntity.ok(stationsDto);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get energy metrics for the company")
    public ResponseEntity<EnergyMetricsDto> getMetrics() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        logger.info("Fetching energy metrics for company: {}", companyId);

        int totalStations = energyQueryService.getTotalStationsByCompany();
        int activeStations = energyQueryService.getActiveStationsByCompany();
        double totalConsumption = energyQueryService.getTodayConsumptionByCompany();
        int activeSessions = energyQueryService.getActiveSessionsByCompany();

        EnergyMetricsDto metrics = EnergyMetricsDto.from(
                totalConsumption,
                totalStations,
                activeStations,
                activeSessions
        );

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get all active alerts for the company")
    public ResponseEntity<List<EnergyAlertDto>> getAlerts() {
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        logger.info("Fetching alerts for company: {}", companyId);

        List<Alert> alerts = energyQueryService.getAlertsByCompany();
        List<EnergyAlertDto> alertsDto = alerts.stream()
                .map(EnergyAlertDto::from)
                .toList();

        return ResponseEntity.ok(alertsDto);
    }

    @GetMapping("/charges")
    @Operation(summary = "Get recent charging sessions for the company")
    public ResponseEntity<List<ChargeSessionDto>> getCharges(
            @RequestParam(defaultValue = "10") @Parameter(description = "Maximum number of sessions to return (1-50)") int limit) {
        
        UUID companyId = CompanyContext.requireCompanyId().getValue();
        logger.info("Fetching recent charges for company: {}, limit: {}", companyId, limit);

        int clampedLimit = Math.max(1, Math.min(50, limit));
        
        List<ChargingSession> sessions = energyQueryService.getRecentSessionsByCompany(clampedLimit);
        List<ChargeSessionDto> sessionsDto = sessions.stream()
                .map(ChargeSessionDto::from)
                .toList();

        return ResponseEntity.ok(sessionsDto);
    }
}