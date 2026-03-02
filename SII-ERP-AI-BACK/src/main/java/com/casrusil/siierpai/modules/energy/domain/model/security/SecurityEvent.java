package com.casrusil.siierpai.modules.energy.domain.model.security;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Security Event for OCPP 1.6 Security Extensions audit logging.
 * 
 * Records security-related events such as authentication failures,
 * firmware updates, certificate changes, etc.
 */
public class SecurityEvent {
    private final UUID id;
    private final String eventType;
    private final LocalDateTime timestamp;
    private final String techInfo;
    private final StationId stationId;
    private final LocalDateTime createdAt;

    private SecurityEvent(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Event ID is required");
        this.eventType = Objects.requireNonNull(builder.eventType, "Event type is required");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "Timestamp is required");
        this.techInfo = builder.techInfo;
        this.stationId = builder.stationId;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
    }

    /**
     * Creates a new security event.
     */
    public static SecurityEvent create(
            String eventType,
            LocalDateTime timestamp,
            String techInfo,
            StationId stationId) {

        return builder()
                .id(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(timestamp)
                .techInfo(techInfo)
                .stationId(stationId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getTechInfo() {
        return techInfo;
    }

    public StationId getStationId() {
        return stationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String eventType;
        private LocalDateTime timestamp;
        private String techInfo;
        private StationId stationId;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder techInfo(String techInfo) {
            this.techInfo = techInfo;
            return this;
        }

        public Builder stationId(StationId stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SecurityEvent build() {
            return new SecurityEvent(this);
        }
    }
}
