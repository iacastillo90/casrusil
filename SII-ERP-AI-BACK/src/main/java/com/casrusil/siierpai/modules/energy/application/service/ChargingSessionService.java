package com.casrusil.siierpai.modules.energy.application.service;

import com.casrusil.siierpai.modules.energy.domain.event.*;
import com.casrusil.siierpai.modules.energy.domain.model.*;
import com.casrusil.siierpai.modules.energy.domain.port.in.*;
import com.casrusil.siierpai.modules.energy.domain.port.out.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de Aplicación: Gestión de Sesiones de Carga.
 * Implementa los casos de uso relacionados con sesiones de carga.
 * 
 * Patrón: Application Service (Hexagonal Architecture)
 * Responsabilidad: Orquestar el dominio y emitir eventos.
 */
@Transactional
@Service
public class ChargingSessionService implements
                InitiateChargingSessionUseCase,
                StopChargingSessionUseCase,
                RecordEnergyConsumptionUseCase {

        private final ChargingStationRepository stationRepository;
        private final ChargingSessionRepository sessionRepository;
        private final EventPublisher eventPublisher;

        public ChargingSessionService(
                        ChargingStationRepository stationRepository,
                        ChargingSessionRepository sessionRepository,
                        EventPublisher eventPublisher) {
                this.stationRepository = stationRepository;
                this.sessionRepository = sessionRepository;
                this.eventPublisher = eventPublisher;
        }

        @Override
        public ChargingSession execute(InitiateSessionCommand command) {
                // 1. Validar que la estación existe
                ChargingStation station = stationRepository.findById(command.stationId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Estación no encontrada: " + command.stationId()));

                // 2. Validar que el conector existe y está disponible
                Connector connector = station.findConnector(command.chargerOcppId(), command.connectorId())
                                .orElseThrow(() -> new IllegalArgumentException("Conector no encontrado"));

                if (!connector.getStatus().isAvailableForCharging()) {
                        throw new IllegalStateException(
                                        "El conector no está disponible. Estado: " + connector.getStatus());
                }

                // 3. Verificar que no hay sesión activa en el conector
                if (sessionRepository.hasActiveSession(command.stationId(), command.chargerOcppId(),
                                command.connectorId())) {
                        throw new IllegalStateException("Ya existe una sesión activa en este conector");
                }

                // 4. Crear la sesión
                ChargingSession session = ChargingSession.builder()
                                .sessionId(SessionId.generate())
                                .stationId(command.stationId())
                                .chargerOcppId(command.chargerOcppId())
                                .connectorId(command.connectorId())
                                .userId(command.userId())
                                .startedAt(LocalDateTime.now())
                                .build();

                // 5. Iniciar la carga en el dominio
                EnergyMeasurement initialMeasurement = EnergyMeasurement.of(
                                command.initialEnergyKwh(),
                                LocalDateTime.now());
                session.startCharging(initialMeasurement);

                // 6. Actualizar el estado del conector
                connector.startCharging();
                connector.markAsCharging();

                // 7. Persistir cambios
                sessionRepository.save(session);
                stationRepository.save(station);

                // 8. Emitir evento de dominio (CRÍTICO para contabilidad)
                ChargingSessionStartedEvent event = new ChargingSessionStartedEvent(
                                session.getSessionId(),
                                session.getStationId(),
                                session.getChargerOcppId(),
                                session.getConnectorId(),
                                session.getUserId(),
                                initialMeasurement);
                eventPublisher.publish(event);

                return session;
        }

        @Override
        public ChargingSession execute(StopSessionCommand command) {
                // 1. Buscar la sesión
                ChargingSession session = sessionRepository.findById(command.sessionId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Sesión no encontrada: " + command.sessionId()));

                if (!session.isActive()) {
                        throw new IllegalStateException("La sesión no está activa");
                }

                // 2. Buscar la estación y el conector
                ChargingStation station = stationRepository.findById(session.getStationId())
                                .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada"));

                Connector connector = station.findConnector(session.getChargerOcppId(), session.getConnectorId())
                                .orElseThrow(() -> new IllegalArgumentException("Conector no encontrado"));

                // 3. Completar la sesión en el dominio
                EnergyMeasurement finalMeasurement = EnergyMeasurement.of(
                                command.finalEnergyKwh(),
                                LocalDateTime.now());
                session.complete(finalMeasurement, command.stopReason());

                // 4. Liberar el conector
                connector.finishCharging();
                connector.release();

                // 5. Persistir cambios
                sessionRepository.save(session);
                stationRepository.save(station);

                // 6. Emitir evento de dominio (CRÍTICO para contabilidad)
                ChargingSessionCompletedEvent event = new ChargingSessionCompletedEvent(
                                session.getSessionId(),
                                session.getStationId(),
                                session.getChargerOcppId(),
                                session.getConnectorId(),
                                session.getUserId(),
                                session.getInitialMeasurement(),
                                session.getFinalMeasurement(),
                                session.getTotalEnergyConsumed(),
                                session.getDuration(),
                                session.getStopReason());
                eventPublisher.publish(event);

                return session;
        }

        @Override
        public void execute(RecordEnergyCommand command) {
                // 1. Buscar la sesión
                ChargingSession session = sessionRepository.findById(command.sessionId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Sesión no encontrada: " + command.sessionId()));

                if (!session.isActive()) {
                        throw new IllegalStateException("La sesión no está activa");
                }

                // 2. Registrar la medición
                EnergyMeasurement measurement = EnergyMeasurement.of(
                                command.energyKwh(),
                                LocalDateTime.now(),
                                command.meterValue());
                session.recordEnergyMeasurement(measurement);

                // 3. Calcular energía acumulada
                var cumulativeEnergy = session.getInitialMeasurement() != null
                                ? measurement.calculateDifference(session.getInitialMeasurement())
                                : command.energyKwh();

                // 4. Persistir
                sessionRepository.save(session);

                // 5. Emitir evento (CRÍTICO para contabilidad en tiempo real)
                EnergyConsumedEvent event = new EnergyConsumedEvent(
                                session.getSessionId(),
                                session.getStationId(),
                                session.getChargerOcppId(),
                                session.getConnectorId(),
                                measurement,
                                cumulativeEnergy);
                eventPublisher.publish(event);
        }
}
