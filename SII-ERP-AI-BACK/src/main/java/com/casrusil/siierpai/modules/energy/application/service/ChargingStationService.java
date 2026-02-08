package com.casrusil.siierpai.modules.energy.application.service;

import com.casrusil.siierpai.modules.energy.domain.event.*;
import com.casrusil.siierpai.modules.energy.domain.model.*;
import com.casrusil.siierpai.modules.energy.domain.port.in.*;
import com.casrusil.siierpai.modules.energy.domain.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio de Aplicación: Gestión de Estaciones de Carga.
 * Implementa los casos de uso relacionados con estaciones y cargadores.
 * 
 * Patrón: Application Service (Hexagonal Architecture)
 */
@Service
public class ChargingStationService implements
                RegisterChargingStationUseCase,
                ProcessOcppHeartbeatUseCase,
                UpdateConnectorStatusUseCase {

        private static final Logger logger = LoggerFactory.getLogger(ChargingStationService.class);

        private final ChargingStationRepository stationRepository;
        private final EventPublisher eventPublisher;

        public ChargingStationService(
                        ChargingStationRepository stationRepository,
                        EventPublisher eventPublisher) {
                this.stationRepository = stationRepository;
                this.eventPublisher = eventPublisher;
        }

        @Override
        public ChargingStation execute(RegisterStationCommand command) {
                // 1. Crear la estación
                ChargingStation station = ChargingStation.builder()
                                .stationId(StationId.generate())
                                .name(command.name())
                                .location(command.location())
                                .companyId(command.companyId())
                                .createdAt(LocalDateTime.now())
                                .status(StationStatus.UNDER_CONSTRUCTION)
                                .active(false)
                                .build();

                // 2. Persistir
                ChargingStation savedStation = stationRepository.save(station);

                // 3. Emitir evento
                StationStatusChangedEvent event = new StationStatusChangedEvent(
                                savedStation.getStationId(),
                                null,
                                StationStatus.UNDER_CONSTRUCTION,
                                "Estación registrada");
                eventPublisher.publish(event);

                return savedStation;
        }

        @Override
        public void execute(HeartbeatCommand command) {
                // 1. Buscar el cargador por OCPP ID
                Charger charger = stationRepository.findChargerByOcppId(command.chargerOcppId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Cargador no encontrado: " + command.chargerOcppId()));

                // 2. Registrar heartbeat en el dominio
                ChargerStatus previousStatus = charger.getStatus();
                charger.recordHeartbeat();

                // 3. Si cambió de OFFLINE a ONLINE, buscar la estación y persistir
                if (previousStatus == ChargerStatus.OFFLINE && charger.getStatus() == ChargerStatus.ONLINE) {
                        // Buscar la estación que contiene este cargador
                        // (En una implementación real, el repositorio debería devolver la estación
                        // junto con el cargador)
                        // Por ahora, asumimos que tenemos acceso a la estación
                        // stationRepository.save(station);
                }
        }

        @Override
        public void execute(UpdateConnectorStatusCommand command) {
                // 1. Buscar la estación
                ChargingStation station = stationRepository.findById(command.stationId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Estación no encontrada: " + command.stationId()));

                // 2. Buscar el conector
                Connector connector = station.findConnector(command.chargerOcppId(), command.connectorId())
                                .orElseThrow(() -> new IllegalArgumentException("Conector no encontrado"));

                // 3. Actualizar estado en el dominio
                ConnectorStatus previousStatus = connector.getStatus();
                connector.changeStatus(command.newStatus());

                // 4. Persistir
                stationRepository.save(station);

                // 5. Emitir evento
                ConnectorStatusChangedEvent event = new ConnectorStatusChangedEvent(
                                command.stationId(),
                                command.chargerOcppId(),
                                command.connectorId(),
                                previousStatus,
                                command.newStatus());
                eventPublisher.publish(event);
        }

        /**
         * Marks a charging station as under maintenance.
         * This will change the station status to MAINTENANCE and prevent new charging
         * sessions.
         */
        public void markStationUnderMaintenance(StationId stationId) {
                logger.info("Marking station {} as under maintenance", stationId);

                ChargingStation station = stationRepository.findById(stationId)
                                .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

                StationStatus previousStatus = station.getStatus();
                station.enterMaintenance();
                stationRepository.save(station);

                // Emit status change event
                StationStatusChangedEvent event = new StationStatusChangedEvent(
                                stationId,
                                previousStatus,
                                StationStatus.MAINTENANCE,
                                "Station entered maintenance mode");
                eventPublisher.publish(event);

                logger.info("Station {} successfully marked as under maintenance", stationId);
        }

        /**
         * Marks a charging station as available after maintenance completion.
         * This will restore the station to OPERATIONAL status.
         */
        public void markStationAvailable(StationId stationId) {
                logger.info("Marking station {} as available after maintenance", stationId);

                ChargingStation station = stationRepository.findById(stationId)
                                .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

                StationStatus previousStatus = station.getStatus();
                station.completeMaintenance();
                stationRepository.save(station);

                // Emit status change event
                StationStatusChangedEvent event = new StationStatusChangedEvent(
                                stationId,
                                previousStatus,
                                StationStatus.OPERATIONAL,
                                "Station maintenance completed, back to operational");
                eventPublisher.publish(event);

                logger.info("Station {} successfully marked as available", stationId);
        }
}
