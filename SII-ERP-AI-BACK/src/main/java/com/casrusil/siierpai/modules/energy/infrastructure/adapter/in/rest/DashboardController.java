package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.energy.application.service.monitoring.AlertService;
import com.casrusil.siierpai.modules.energy.application.service.monitoring.DashboardService;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for real-time monitoring and dashboard.
 */
// P0 multiempresa: apagado por defecto porque sus tablas (alertas, mantenciones, precios, campañas, catálogo) no tienen company_id.
// Sin el bean, sus rutas responden 404 como cualquier ruta inexistente. Ver ronda P6.
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "app.features.energy-admin", name = "enabled", havingValue = "true", matchIfMissing = false)
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard & Monitoring", description = "Real-time metrics, alerts, and system monitoring")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final AlertService alertService;

    public DashboardController(DashboardService dashboardService, AlertService alertService) {
        this.dashboardService = dashboardService;
        this.alertService = alertService;
    }

    // ==================== Dashboard Metrics ====================

    @GetMapping("/metrics")
    @Operation(summary = "Get current dashboard metrics", description = "Returns real-time dashboard metrics snapshot")
    public ResponseEntity<DashboardMetricsDto> getCurrentMetrics() {
        logger.info("Fetching current dashboard metrics");

        DashboardMetrics metrics = dashboardService.getCurrentMetrics();

        return ResponseEntity.ok(DashboardMetricsDto.from(metrics));
    }

    @GetMapping("/metrics/{metricType}")
    @Operation(summary = "Get specific metric value")
    public ResponseEntity<MetricValueDto> getMetricValue(@PathVariable MetricType metricType) {
        logger.info("Fetching metric: {}", metricType);

        BigDecimal value = dashboardService.getMetricValue(metricType);

        return ResponseEntity.ok(new MetricValueDto(
                metricType.name(),
                metricType.getDisplayName(),
                value,
                metricType.getUnit(),
                LocalDateTime.now()));
    }

    // ==================== Alerts ====================

    @GetMapping("/alerts")
    @Operation(summary = "Get all active alerts")
    public ResponseEntity<List<AlertDto>> getActiveAlerts() {
        logger.info("Fetching active alerts");

        List<Alert> alerts = alertService.getActiveAlerts();

        return ResponseEntity.ok(alerts.stream()
                .map(AlertDto::from)
                .toList());
    }

    @GetMapping("/alerts/unacknowledged")
    @Operation(summary = "Get unacknowledged alerts")
    public ResponseEntity<List<AlertDto>> getUnacknowledgedAlerts() {
        logger.info("Fetching unacknowledged alerts");

        List<Alert> alerts = alertService.getUnacknowledgedAlerts();

        return ResponseEntity.ok(alerts.stream()
                .map(AlertDto::from)
                .toList());
    }

    @GetMapping("/alerts/severity/{severity}")
    @Operation(summary = "Get alerts by severity")
    public ResponseEntity<List<AlertDto>> getAlertsBySeverity(@PathVariable AlertSeverity severity) {
        logger.info("Fetching alerts with severity: {}", severity);

        List<Alert> alerts = alertService.getAlertsBySeverity(severity);

        return ResponseEntity.ok(alerts.stream()
                .map(AlertDto::from)
                .toList());
    }

    @GetMapping("/alerts/station/{stationId}")
    @Operation(summary = "Get alerts for a specific station")
    public ResponseEntity<List<AlertDto>> getStationAlerts(@PathVariable UUID stationId) {
        logger.info("Fetching alerts for station: {}", stationId);

        List<Alert> alerts = alertService.getStationAlerts(StationId.of(stationId));

        return ResponseEntity.ok(alerts.stream()
                .map(AlertDto::from)
                .toList());
    }

    @PostMapping("/alerts")
    @Operation(summary = "Create a new alert")
    public ResponseEntity<AlertDto> createAlert(@RequestBody @Valid CreateAlertRequest request) {
        logger.info("Creating alert: {}", request.type());

        Alert alert;
        if (request.stationId() != null) {
            alert = alertService.createStationAlert(
                    request.type(),
                    request.severity(),
                    request.title(),
                    request.message(),
                    StationId.of(request.stationId()));
        } else {
            alert = alertService.createAlert(
                    request.type(),
                    request.severity(),
                    request.title(),
                    request.message());
        }

        return ResponseEntity.ok(AlertDto.from(alert));
    }

    @PutMapping("/alerts/{id}/acknowledge")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<AlertDto> acknowledgeAlert(
            @PathVariable UUID id,
            @RequestBody @Valid AcknowledgeAlertRequest request) {

        logger.info("Acknowledging alert: {}", id);

        try {
            Alert alert = alertService.acknowledgeAlert(AlertId.of(id), request.userId());
            return ResponseEntity.ok(AlertDto.from(alert));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/alerts/{id}/resolve")
    @Operation(summary = "Resolve an alert")
    public ResponseEntity<AlertDto> resolveAlert(@PathVariable UUID id) {
        logger.info("Resolving alert: {}", id);

        try {
            Alert alert = alertService.resolveAlert(AlertId.of(id));
            return ResponseEntity.ok(AlertDto.from(alert));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/alerts/statistics")
    @Operation(summary = "Get alert statistics")
    public ResponseEntity<AlertService.AlertStatistics> getAlertStatistics() {
        logger.info("Fetching alert statistics");

        return ResponseEntity.ok(alertService.getStatistics());
    }

    // ==================== DTOs ====================

    public record DashboardMetricsDto(
            UUID id,
            LocalDateTime timestamp,
            int activeSessions,
            int totalSessionsToday,
            int completedSessionsToday,
            BigDecimal totalEnergyToday,
            BigDecimal currentPowerConsumption,
            BigDecimal averageEnergyPerSession,
            BigDecimal totalRevenueToday,
            BigDecimal averageRevenuePerSession,
            int activeStations,
            int offlineStations,
            int totalStations,
            BigDecimal stationUtilizationRate,
            int averageSessionDurationMinutes,
            BigDecimal peakPowerToday) {
        public static DashboardMetricsDto from(DashboardMetrics metrics) {
            return new DashboardMetricsDto(
                    metrics.getId().value(),
                    metrics.getTimestamp(),
                    metrics.getActiveSessions(),
                    metrics.getTotalSessionsToday(),
                    metrics.getCompletedSessionsToday(),
                    metrics.getTotalEnergyToday(),
                    metrics.getCurrentPowerConsumption(),
                    metrics.getAverageEnergyPerSession(),
                    metrics.getTotalRevenueToday(),
                    metrics.getAverageRevenuePerSession(),
                    metrics.getActiveStations(),
                    metrics.getOfflineStations(),
                    metrics.getTotalStations(),
                    metrics.getStationUtilizationRate(),
                    metrics.getAverageSessionDurationMinutes(),
                    metrics.getPeakPowerToday());
        }
    }

    public record MetricValueDto(
            String metricType,
            String displayName,
            BigDecimal value,
            String unit,
            LocalDateTime timestamp) {
    }

    public record AlertDto(
            UUID id,
            String type,
            String severity,
            String title,
            String message,
            UUID stationId,
            boolean acknowledged,
            LocalDateTime acknowledgedAt,
            UUID acknowledgedBy,
            LocalDateTime createdAt,
            LocalDateTime resolvedAt,
            boolean active) {
        public static AlertDto from(Alert alert) {
            return new AlertDto(
                    alert.getId().value(),
                    alert.getType().name(),
                    alert.getSeverity().name(),
                    alert.getTitle(),
                    alert.getMessage(),
                    alert.getStationId() != null ? alert.getStationId().getValue() : null,
                    alert.isAcknowledged(),
                    alert.getAcknowledgedAt(),
                    alert.getAcknowledgedBy(),
                    alert.getCreatedAt(),
                    alert.getResolvedAt(),
                    alert.isActive());
        }
    }

    public record CreateAlertRequest(
            @NotNull AlertType type,
            @NotNull AlertSeverity severity,
            @NotBlank String title,
            @NotBlank String message,
            UUID stationId) {
    }

    public record AcknowledgeAlertRequest(
            @NotNull UUID userId) {
    }
}
