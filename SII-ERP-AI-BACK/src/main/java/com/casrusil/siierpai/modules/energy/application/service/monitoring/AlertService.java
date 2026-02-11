package com.casrusil.siierpai.modules.energy.application.service.monitoring;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.Alert;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertId;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertSeverity;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing system alerts.
 */
@Service
@Transactional
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    // In-memory storage for now (TODO: replace with repository)
    private final Map<AlertId, Alert> alerts = new ConcurrentHashMap<>();
    private final WebSocketEventPublisher eventPublisher;

    public AlertService(WebSocketEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new alert.
     */
    public Alert createAlert(AlertType type, AlertSeverity severity, String title, String message) {
        logger.info("Creating alert: {} - {}", type, title);

        Alert alert = Alert.create(type, severity, title, message);
        alerts.put(alert.getId(), alert);

        // Publish via WebSocket
        eventPublisher.publishAlert(alert);

        logger.info("Alert created: {}", alert.getId());
        return alert;
    }

    /**
     * Creates an alert for a specific station.
     */
    public Alert createStationAlert(
            AlertType type,
            AlertSeverity severity,
            String title,
            String message,
            StationId stationId) {

        logger.info("Creating station alert for {}: {} - {}", stationId, type, title);

        Alert alert = Alert.createForStation(type, severity, title, message, stationId);
        alerts.put(alert.getId(), alert);

        // Publish via WebSocket
        eventPublisher.publishAlert(alert);

        logger.info("Station alert created: {}", alert.getId());
        return alert;
    }

    /**
     * Acknowledges an alert.
     */
    public Alert acknowledgeAlert(AlertId alertId, UUID userId) {
        logger.info("Acknowledging alert: {} by user: {}", alertId, userId);

        Alert alert = alerts.get(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("Alert not found: " + alertId);
        }

        alert.acknowledge(userId);

        // Publish via WebSocket
        eventPublisher.publishAlertAcknowledged(alert);

        return alert;
    }

    /**
     * Resolves an alert.
     */
    public Alert resolveAlert(AlertId alertId) {
        logger.info("Resolving alert: {}", alertId);

        Alert alert = alerts.get(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("Alert not found: " + alertId);
        }

        alert.resolve();

        // Publish via WebSocket
        eventPublisher.publishAlertResolved(alert);

        return alert;
    }

    /**
     * Gets all active (unresolved) alerts.
     */
    public List<Alert> getActiveAlerts() {
        return alerts.values().stream()
                .filter(Alert::isActive)
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all unacknowledged alerts.
     */
    public List<Alert> getUnacknowledgedAlerts() {
        return alerts.values().stream()
                .filter(alert -> !alert.isAcknowledged() && alert.isActive())
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Gets alerts by severity.
     */
    public List<Alert> getAlertsBySeverity(AlertSeverity severity) {
        return alerts.values().stream()
                .filter(alert -> alert.getSeverity() == severity && alert.isActive())
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Gets alerts for a specific station.
     */
    public List<Alert> getStationAlerts(StationId stationId) {
        return alerts.values().stream()
                .filter(alert -> stationId.equals(alert.getStationId()) && alert.isActive())
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Gets alert statistics.
     */
    public AlertStatistics getStatistics() {
        long total = alerts.size();
        long active = alerts.values().stream().filter(Alert::isActive).count();
        long critical = alerts.values().stream()
                .filter(a -> a.getSeverity() == AlertSeverity.CRITICAL && a.isActive())
                .count();
        long unacknowledged = alerts.values().stream()
                .filter(a -> !a.isAcknowledged() && a.isActive())
                .count();

        return new AlertStatistics(total, active, critical, unacknowledged);
    }

    /**
     * Record for alert statistics.
     */
    public record AlertStatistics(
            long totalAlerts,
            long activeAlerts,
            long criticalAlerts,
            long unacknowledgedAlerts) {
    }
}
