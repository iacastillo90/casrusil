package com.casrusil.siierpai.modules.energy.application.service.monitoring;

import com.casrusil.siierpai.modules.energy.domain.model.monitoring.Alert;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.DashboardMetrics;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.MetricSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing real-time updates via WebSocket.
 */
@Service
public class WebSocketEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Publishes dashboard metrics update.
     */
    public void publishMetricsUpdate(DashboardMetrics metrics) {
        logger.debug("Publishing metrics update via WebSocket");

        try {
            messagingTemplate.convertAndSend("/topic/dashboard/metrics", metrics);
        } catch (Exception e) {
            logger.error("Failed to publish metrics update", e);
        }
    }

    /**
     * Publishes a single metric snapshot.
     */
    public void publishMetricSnapshot(MetricSnapshot snapshot) {
        logger.debug("Publishing metric snapshot: {}", snapshot.metricName());

        try {
            messagingTemplate.convertAndSend("/topic/metrics/" + snapshot.metricName(), snapshot);
        } catch (Exception e) {
            logger.error("Failed to publish metric snapshot", e);
        }
    }

    /**
     * Publishes new alert.
     */
    public void publishAlert(Alert alert) {
        logger.info("Publishing alert via WebSocket: {} - {}", alert.getSeverity(), alert.getTitle());

        try {
            messagingTemplate.convertAndSend("/topic/alerts", alert);

            // Also send to severity-specific topic
            messagingTemplate.convertAndSend(
                    "/topic/alerts/" + alert.getSeverity().name().toLowerCase(),
                    alert);
        } catch (Exception e) {
            logger.error("Failed to publish alert", e);
        }
    }

    /**
     * Publishes alert acknowledgment.
     */
    public void publishAlertAcknowledged(Alert alert) {
        logger.debug("Publishing alert acknowledgment: {}", alert.getId());

        try {
            messagingTemplate.convertAndSend("/topic/alerts/acknowledged", alert);
        } catch (Exception e) {
            logger.error("Failed to publish alert acknowledgment", e);
        }
    }

    /**
     * Publishes alert resolution.
     */
    public void publishAlertResolved(Alert alert) {
        logger.debug("Publishing alert resolution: {}", alert.getId());

        try {
            messagingTemplate.convertAndSend("/topic/alerts/resolved", alert);
        } catch (Exception e) {
            logger.error("Failed to publish alert resolution", e);
        }
    }

    /**
     * Publishes session event.
     */
    public void publishSessionEvent(String eventType, Object sessionData) {
        logger.debug("Publishing session event: {}", eventType);

        try {
            messagingTemplate.convertAndSend("/topic/sessions/" + eventType, sessionData);
        } catch (Exception e) {
            logger.error("Failed to publish session event", e);
        }
    }

    /**
     * Publishes station status change.
     */
    public void publishStationStatusChange(String stationId, String status) {
        logger.debug("Publishing station status change: {} -> {}", stationId, status);

        try {
            var statusUpdate = new StationStatusUpdate(stationId, status, System.currentTimeMillis());
            messagingTemplate.convertAndSend("/topic/stations/status", statusUpdate);
        } catch (Exception e) {
            logger.error("Failed to publish station status change", e);
        }
    }

    /**
     * Record for station status updates.
     */
    public record StationStatusUpdate(
            String stationId,
            String status,
            long timestamp) {
    }
}
