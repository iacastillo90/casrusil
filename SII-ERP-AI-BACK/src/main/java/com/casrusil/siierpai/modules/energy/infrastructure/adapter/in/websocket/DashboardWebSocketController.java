package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.websocket;

import com.casrusil.siierpai.modules.energy.application.service.monitoring.DashboardService;
import com.casrusil.siierpai.modules.energy.application.service.monitoring.WebSocketEventPublisher;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.DashboardMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time dashboard updates.
 */
@Controller
public class DashboardWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardWebSocketController.class);

    private final DashboardService dashboardService;
    private final WebSocketEventPublisher eventPublisher;

    public DashboardWebSocketController(
            DashboardService dashboardService,
            WebSocketEventPublisher eventPublisher) {
        this.dashboardService = dashboardService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles request for current metrics.
     */
    @MessageMapping("/dashboard/metrics/request")
    @SendTo("/topic/dashboard/metrics")
    public DashboardMetrics requestMetrics() {
        logger.debug("Received WebSocket request for dashboard metrics");
        return dashboardService.getCurrentMetrics();
    }

    /**
     * Periodically broadcasts dashboard metrics (every 5 seconds).
     */
    @Scheduled(fixedRate = 5000)
    public void broadcastMetrics() {
        try {
            DashboardMetrics metrics = dashboardService.getCurrentMetrics();
            eventPublisher.publishMetricsUpdate(metrics);
        } catch (Exception e) {
            logger.error("Failed to broadcast metrics", e);
        }
    }

    /**
     * Periodically broadcasts metrics every 30 seconds for less frequent updates.
     */
    @Scheduled(fixedRate = 30000)
    public void broadcastDetailedMetrics() {
        try {
            DashboardMetrics metrics = dashboardService.getCurrentMetrics();
            logger.debug("Broadcasting detailed metrics: {} active sessions",
                    metrics.getActiveSessions());
        } catch (Exception e) {
            logger.error("Failed to broadcast detailed metrics", e);
        }
    }
}
