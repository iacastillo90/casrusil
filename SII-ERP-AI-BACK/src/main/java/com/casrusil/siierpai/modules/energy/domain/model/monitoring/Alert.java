package com.casrusil.siierpai.modules.energy.domain.model.monitoring;

import com.casrusil.siierpai.modules.energy.domain.model.SessionId;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root representing a system alert.
 */
public class Alert {
    private final AlertId id;
    private final AlertType type;
    private final AlertSeverity severity;
    private final String title;
    private final String message;
    private final StationId stationId;
    private final SessionId sessionId;
    private boolean acknowledged;
    private LocalDateTime acknowledgedAt;
    private UUID acknowledgedBy;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    private Alert(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Alert ID is required");
        this.type = Objects.requireNonNull(builder.type, "Alert type is required");
        this.severity = Objects.requireNonNull(builder.severity, "Severity is required");
        this.title = Objects.requireNonNull(builder.title, "Title is required");
        this.message = Objects.requireNonNull(builder.message, "Message is required");
        this.stationId = builder.stationId;
        this.sessionId = builder.sessionId;
        this.acknowledged = builder.acknowledged;
        this.acknowledgedAt = builder.acknowledgedAt;
        this.acknowledgedBy = builder.acknowledgedBy;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
        this.resolvedAt = builder.resolvedAt;
    }

    /**
     * Creates a new alert.
     */
    public static Alert create(AlertType type, AlertSeverity severity, String title, String message) {
        return builder()
                .id(AlertId.generate())
                .type(type)
                .severity(severity)
                .title(title)
                .message(message)
                .acknowledged(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an alert for a specific station.
     */
    public static Alert createForStation(
            AlertType type,
            AlertSeverity severity,
            String title,
            String message,
            StationId stationId) {
        return builder()
                .id(AlertId.generate())
                .type(type)
                .severity(severity)
                .title(title)
                .message(message)
                .stationId(stationId)
                .acknowledged(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Acknowledges the alert.
     */
    public void acknowledge(UUID userId) {
        if (this.acknowledged) {
            throw new IllegalStateException("Alert already acknowledged");
        }
        this.acknowledged = true;
        this.acknowledgedAt = LocalDateTime.now();
        this.acknowledgedBy = userId;
    }

    /**
     * Resolves the alert.
     */
    public void resolve() {
        if (this.resolvedAt != null) {
            throw new IllegalStateException("Alert already resolved");
        }
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Checks if alert is active (not resolved).
     */
    public boolean isActive() {
        return resolvedAt == null;
    }

    // Getters
    public AlertId getId() {
        return id;
    }

    public AlertType getType() {
        return type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public StationId getStationId() {
        return stationId;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public UUID getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AlertId id;
        private AlertType type;
        private AlertSeverity severity;
        private String title;
        private String message;
        private StationId stationId;
        private SessionId sessionId;
        private boolean acknowledged;
        private LocalDateTime acknowledgedAt;
        private UUID acknowledgedBy;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;

        public Builder id(AlertId id) {
            this.id = id;
            return this;
        }

        public Builder type(AlertType type) {
            this.type = type;
            return this;
        }

        public Builder severity(AlertSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder stationId(StationId stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder sessionId(SessionId sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder acknowledged(boolean acknowledged) {
            this.acknowledged = acknowledged;
            return this;
        }

        public Builder acknowledgedAt(LocalDateTime acknowledgedAt) {
            this.acknowledgedAt = acknowledgedAt;
            return this;
        }

        public Builder acknowledgedBy(UUID acknowledgedBy) {
            this.acknowledgedBy = acknowledgedBy;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder resolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public Alert build() {
            return new Alert(this);
        }
    }
}
