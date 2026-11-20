package com.casrusil.siierpai.modules.energy.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de Dominio: Sesión de Carga.
 * Representa una sesión completa de carga de un vehículo eléctrico.
 * Puede ser un Agregado Raíz independiente o parte del agregado
 * ChargingStation.
 */
public class ChargingSession {

    private final SessionId sessionId;
    private final StationId stationId;
    private final OcppIdentifier chargerOcppId;
    private final ConnectorId connectorId;
    private final UUID userId; // Usuario que inició la sesión
    private final LocalDateTime startedAt;

    private ChargingSessionStatus status;
    private LocalDateTime endedAt;
    private EnergyMeasurement initialMeasurement;
    private EnergyMeasurement finalMeasurement;
    private final List<EnergyMeasurement> intermediateMeasurements;
    private String stopReason;

    private ChargingSession(Builder builder) {
        this.sessionId = Objects.requireNonNull(builder.sessionId, "SessionId no puede ser nulo");
        this.stationId = Objects.requireNonNull(builder.stationId, "StationId no puede ser nulo");
        this.chargerOcppId = Objects.requireNonNull(builder.chargerOcppId, "ChargerOcppId no puede ser nulo");
        this.connectorId = Objects.requireNonNull(builder.connectorId, "ConnectorId no puede ser nulo");
        this.userId = Objects.requireNonNull(builder.userId, "UserId no puede ser nulo");
        this.startedAt = Objects.requireNonNull(builder.startedAt, "StartedAt no puede ser nulo");
        this.status = builder.status != null ? builder.status : ChargingSessionStatus.INITIATED;
        this.intermediateMeasurements = new ArrayList<>();
    }

    // ========== COMPORTAMIENTO DE DOMINIO ==========

    /**
     * Inicia la carga activa.
     */
    public void startCharging(EnergyMeasurement initialMeasurement) {
        if (this.status != ChargingSessionStatus.INITIATED) {
            throw new IllegalStateException("La sesión ya fue iniciada");
        }
        this.initialMeasurement = Objects.requireNonNull(initialMeasurement, "Medición inicial requerida");
        this.status = ChargingSessionStatus.CHARGING;
    }

    /**
     * Registra una medición intermedia de energía.
     */
    public void recordEnergyMeasurement(EnergyMeasurement measurement) {
        Objects.requireNonNull(measurement, "La medición no puede ser nula");

        if (this.status != ChargingSessionStatus.CHARGING && this.status != ChargingSessionStatus.SUSPENDED) {
            throw new IllegalStateException("No se pueden registrar mediciones en estado: " + status);
        }

        intermediateMeasurements.add(measurement);
    }

    /**
     * Suspende la sesión de carga.
     */
    public void suspend() {
        if (this.status != ChargingSessionStatus.CHARGING) {
            throw new IllegalStateException("Solo se puede suspender una sesión activa");
        }
        this.status = ChargingSessionStatus.SUSPENDED;
    }

    /**
     * Reanuda la sesión de carga.
     */
    public void resume() {
        if (this.status != ChargingSessionStatus.SUSPENDED) {
            throw new IllegalStateException("Solo se puede reanudar una sesión suspendida");
        }
        this.status = ChargingSessionStatus.CHARGING;
    }

    /**
     * Finaliza la sesión de carga.
     */
    public void complete(EnergyMeasurement finalMeasurement, String reason) {
        if (this.status == ChargingSessionStatus.COMPLETED || this.status == ChargingSessionStatus.FAILED) {
            throw new IllegalStateException("La sesión ya fue finalizada");
        }

        this.finalMeasurement = Objects.requireNonNull(finalMeasurement, "Medición final requerida");
        this.endedAt = LocalDateTime.now();
        this.stopReason = reason;
        this.status = ChargingSessionStatus.COMPLETED;
    }

    /**
     * Marca la sesión como fallida.
     */
    public void fail(String reason) {
        this.endedAt = LocalDateTime.now();
        this.stopReason = reason;
        this.status = ChargingSessionStatus.FAILED;
    }

    /**
     * Calcula el total de energía consumida (kWh).
     */
    public BigDecimal getTotalEnergyConsumed() {
        if (initialMeasurement == null || finalMeasurement == null) {
            return BigDecimal.ZERO;
        }
        return finalMeasurement.calculateDifference(initialMeasurement);
    }

    /**
     * Calcula la duración de la sesión.
     */
    public Duration getDuration() {
        LocalDateTime end = endedAt != null ? endedAt : LocalDateTime.now();
        return Duration.between(startedAt, end);
    }

    /**
     * Calcula el costo estimado de la sesión (requiere tarifa).
     */
    public BigDecimal calculateCost(BigDecimal pricePerKwh) {
        Objects.requireNonNull(pricePerKwh, "El precio por kWh no puede ser nulo");
        return getTotalEnergyConsumed().multiply(pricePerKwh);
    }

    /**
     * Verifica si la sesión está activa.
     */
    public boolean isActive() {
        return status == ChargingSessionStatus.CHARGING || status == ChargingSessionStatus.SUSPENDED;
    }

    /**
     * Verifica si la sesión está completada.
     */
    public boolean isCompleted() {
        return status == ChargingSessionStatus.COMPLETED;
    }

    /**
     * Rehidrata una sesión desde persistencia sin re-ejecutar las transiciones
     * de dominio (que fallarían para sesiones ya iniciadas o completadas).
     *
     * @param startedAt    inicio real
     * @param endedAt      fin real (null si sigue activa)
     * @param status       estado persistido
     * @param initial      medición inicial (puede ser null)
     * @param last         medición final o última conocida (puede ser null)
     * @return sesión reconstruida
     */
    public static ChargingSession restore(
            SessionId sessionId,
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId,
            UUID userId,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            ChargingSessionStatus status,
            EnergyMeasurement initial,
            EnergyMeasurement last) {
        ChargingSession session = new ChargingSession(builder()
                .sessionId(sessionId)
                .stationId(stationId)
                .chargerOcppId(chargerOcppId)
                .connectorId(connectorId)
                .userId(userId)
                .startedAt(startedAt)
                .status(status != null ? status : ChargingSessionStatus.INITIATED));
        session.endedAt = endedAt;
        session.initialMeasurement = initial;
        if (status == ChargingSessionStatus.COMPLETED) {
            session.finalMeasurement = last;
        }
        return session;
    }

    /** Agrega una medición histórica al rehidratar desde persistencia. */
    public void addRestoredMeasurement(EnergyMeasurement measurement) {
        if (measurement != null) {
            intermediateMeasurements.add(measurement);
        }
    }

    // ========== GETTERS ==========

    public SessionId getSessionId() {
        return sessionId;
    }

    public StationId getStationId() {
        return stationId;
    }

    public OcppIdentifier getChargerOcppId() {
        return chargerOcppId;
    }

    public ConnectorId getConnectorId() {
        return connectorId;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public ChargingSessionStatus getStatus() {
        return status;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public EnergyMeasurement getInitialMeasurement() {
        return initialMeasurement;
    }

    public EnergyMeasurement getFinalMeasurement() {
        return finalMeasurement;
    }

    public List<EnergyMeasurement> getIntermediateMeasurements() {
        return Collections.unmodifiableList(intermediateMeasurements);
    }

    public String getStopReason() {
        return stopReason;
    }

    // ========== BUILDER PATTERN ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SessionId sessionId;
        private StationId stationId;
        private OcppIdentifier chargerOcppId;
        private ConnectorId connectorId;
        private UUID userId;
        private LocalDateTime startedAt;
        private ChargingSessionStatus status;

        public Builder sessionId(SessionId sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder stationId(StationId stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder chargerOcppId(OcppIdentifier chargerOcppId) {
            this.chargerOcppId = chargerOcppId;
            return this;
        }

        public Builder connectorId(ConnectorId connectorId) {
            this.connectorId = connectorId;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder status(ChargingSessionStatus status) {
            this.status = status;
            return this;
        }

        public ChargingSession build() {
            return new ChargingSession(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChargingSession that = (ChargingSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return String.format("ChargingSession[%s, %s, %s, %.2f kWh]",
                sessionId, status, getDuration(), getTotalEnergyConsumed());
    }
}
