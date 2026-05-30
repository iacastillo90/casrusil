package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.handler;

import com.casrusil.siierpai.modules.energy.domain.model.*;
import com.casrusil.siierpai.modules.energy.domain.port.in.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.firmware.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Despachador de mensajes OCPP a casos de uso del dominio.
 * 
 * Patrón: Adapter (Hexagonal Architecture)
 * Responsabilidad: Traducir mensajes OCPP a comandos de dominio.
 */
@Component
public class OcppMessageDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(OcppMessageDispatcher.class);

    private final ProcessOcppHeartbeatUseCase heartbeatUseCase;
    private final UpdateConnectorStatusUseCase statusUseCase;
    private final RecordEnergyConsumptionUseCase energyUseCase;
    private final InitiateChargingSessionUseCase initiateSessionUseCase;
    private final StopChargingSessionUseCase stopSessionUseCase;
    private final ObjectMapper objectMapper;
    private final com.casrusil.siierpai.modules.energy.application.service.OcppCommandService commandService;
    private final com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.mapping.OcppStationMappingService stationMappingService;
    private final com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.mapping.OcppTransactionMappingService transactionMappingService;
    private final com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.validation.RfidAuthorizationService rfidAuthService;

    public OcppMessageDispatcher(
            ProcessOcppHeartbeatUseCase heartbeatUseCase,
            UpdateConnectorStatusUseCase statusUseCase,
            RecordEnergyConsumptionUseCase energyUseCase,
            InitiateChargingSessionUseCase initiateSessionUseCase,
            StopChargingSessionUseCase stopSessionUseCase,
            ObjectMapper objectMapper,
            com.casrusil.siierpai.modules.energy.application.service.OcppCommandService commandService,
            com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.mapping.OcppStationMappingService stationMappingService,
            com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.mapping.OcppTransactionMappingService transactionMappingService,
            com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.validation.RfidAuthorizationService rfidAuthService) {
        this.heartbeatUseCase = heartbeatUseCase;
        this.statusUseCase = statusUseCase;
        this.energyUseCase = energyUseCase;
        this.initiateSessionUseCase = initiateSessionUseCase;
        this.stopSessionUseCase = stopSessionUseCase;
        this.objectMapper = objectMapper;
        this.commandService = commandService;
        this.stationMappingService = stationMappingService;
        this.transactionMappingService = transactionMappingService;
        this.rfidAuthService = rfidAuthService;
    }

    /**
     * Completa un comando pendiente con la respuesta del cargador.
     */
    public void completeCommand(String uniqueId, com.fasterxml.jackson.databind.JsonNode payload) {
        commandService.completeCommand(uniqueId, payload);
    }

    /**
     * Falla un comando pendiente con un error.
     */
    public void failCommand(String uniqueId, String errorCode, String errorDescription) {
        commandService.failCommand(uniqueId, errorCode, errorDescription);
    }

    /**
     * Despacha un mensaje OCPP al handler correspondiente.
     */
    public Object dispatch(String action, JsonNode payload, String ocppId) throws Exception {
        logger.debug("Despachando acción: {} para {}", action, ocppId);

        return switch (action) {
            case "BootNotification" -> handleBootNotification(payload, ocppId);
            case "Heartbeat" -> handleHeartbeat(ocppId);
            case "StatusNotification" -> handleStatusNotification(payload, ocppId);
            case "MeterValues" -> handleMeterValues(payload, ocppId);
            case "StartTransaction" -> handleStartTransaction(payload, ocppId);
            case "StopTransaction" -> handleStopTransaction(payload, ocppId);
            case "Authorize" -> handleAuthorize(payload, ocppId);
            case "DiagnosticsStatusNotification" -> handleDiagnosticsStatusNotification(payload, ocppId);
            case "FirmwareStatusNotification" -> handleFirmwareStatusNotification(payload, ocppId);
            default -> throw new UnsupportedOperationException("Acción no soportada: " + action);
        };
    }

    private BootNotificationResponse handleBootNotification(JsonNode payload, String ocppId) throws Exception {
        BootNotificationRequest request = objectMapper.treeToValue(payload, BootNotificationRequest.class);

        logger.info("BootNotification de {}: {} {}",
                ocppId, request.chargePointVendor(), request.chargePointModel());

        // Registrar heartbeat
        heartbeatUseCase.execute(new ProcessOcppHeartbeatUseCase.HeartbeatCommand(
                OcppIdentifier.of(ocppId)));

        return new BootNotificationResponse(
                "Accepted",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                60 // Heartbeat cada 60 segundos
        );
    }

    private HeartbeatResponse handleHeartbeat(String ocppId) {
        heartbeatUseCase.execute(new ProcessOcppHeartbeatUseCase.HeartbeatCommand(
                OcppIdentifier.of(ocppId)));

        return new HeartbeatResponse(
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    private StatusNotificationResponse handleStatusNotification(JsonNode payload, String ocppId) throws Exception {
        StatusNotificationRequest request = objectMapper.treeToValue(payload, StatusNotificationRequest.class);

        logger.info("StatusNotification de {} - Conector {}: {}",
                ocppId, request.connectorId(), request.status());

        // Mapear status OCPP a ConnectorStatus del dominio
        ConnectorStatus domainStatus = mapOcppStatusToDomain(request.status());

        // Buscar StationId por OCPP ID
        stationMappingService.findStationIdByOcppId(OcppIdentifier.of(ocppId))
                .ifPresentOrElse(
                        stationId -> {
                            try {
                                statusUseCase.execute(new UpdateConnectorStatusUseCase.UpdateConnectorStatusCommand(
                                        stationId,
                                        OcppIdentifier.of(ocppId),
                                        ConnectorId.of(request.connectorId()),
                                        domainStatus));
                                logger.debug("Estado del conector actualizado: {}", domainStatus);
                            } catch (Exception e) {
                                logger.error("Error actualizando estado del conector", e);
                            }
                        },
                        () -> logger.warn("Station not found for ocppId: {}", ocppId));

        return new StatusNotificationResponse();
    }

    private MeterValuesResponse handleMeterValues(JsonNode payload, String ocppId) throws Exception {
        MeterValuesRequest request = objectMapper.treeToValue(payload, MeterValuesRequest.class);

        logger.debug("MeterValues de {} - Conector {}", ocppId, request.connectorId());

        // Extraer energía de los valores muestreados
        BigDecimal energyKwh = extractEnergyFromMeterValues(request);

        if (energyKwh != null && request.transactionId() != null) {
            // Buscar SessionId por transactionId
            transactionMappingService.findSessionIdByTransactionId(request.transactionId())
                    .ifPresentOrElse(
                            sessionId -> {
                                try {
                                    energyUseCase.execute(new RecordEnergyConsumptionUseCase.RecordEnergyCommand(
                                            sessionId,
                                            energyKwh,
                                            payload.toString()));
                                    logger.debug("Energía registrada: {} kWh para sesión {}", energyKwh, sessionId);
                                } catch (Exception e) {
                                    logger.error("Error registrando energía", e);
                                }
                            },
                            () -> logger.warn("Session not found for transactionId: {}", request.transactionId()));
        }

        return new MeterValuesResponse();
    }

    private StartTransactionResponse handleStartTransaction(JsonNode payload, String ocppId) throws Exception {
        StartTransactionRequest request = objectMapper.treeToValue(payload, StartTransactionRequest.class);

        logger.info("StartTransaction de {} - Conector {}, IdTag: {}",
                ocppId, request.connectorId(), request.idTag());

        // 1. Validar autorización RFID
        var authInfo = rfidAuthService.authorize(request.idTag());
        if (!"Accepted".equals(authInfo.status())) {
            logger.warn("Autorización rechazada para idTag: {} - Status: {}", request.idTag(), authInfo.status());
            return new StartTransactionResponse(authInfo, 0);
        }

        // 2. Buscar StationId por OCPP ID
        StationId stationId = stationMappingService.findStationIdByOcppId(OcppIdentifier.of(ocppId))
                .orElseThrow(() -> new IllegalStateException("Station not found for ocppId: " + ocppId));

        // 3. Iniciar sesión de carga
        try {
            // TODO: Obtener userId real del sistema de autorización
            UUID userId = UUID.randomUUID(); // Temporal: mapear desde idTag
            BigDecimal initialEnergy = BigDecimal.valueOf(request.meterStart());

            InitiateChargingSessionUseCase.InitiateSessionCommand command = new InitiateChargingSessionUseCase.InitiateSessionCommand(
                    stationId,
                    OcppIdentifier.of(ocppId),
                    ConnectorId.of(request.connectorId()),
                    userId,
                    initialEnergy);

            ChargingSession session = initiateSessionUseCase.execute(command);
            SessionId sessionId = session.getSessionId();

            // 4. Crear mapeo sessionId → transactionId
            int transactionId = transactionMappingService.createTransactionId(sessionId);

            logger.info("Sesión iniciada: sessionId={}, transactionId={}", sessionId, transactionId);

            return new StartTransactionResponse(
                    new IdTagInfo("Accepted", null, null),
                    transactionId);
        } catch (Exception e) {
            logger.error("Error iniciando sesión", e);
            return new StartTransactionResponse(
                    new IdTagInfo("Blocked", null, null),
                    0);
        }
    }

    private StopTransactionResponse handleStopTransaction(JsonNode payload, String ocppId) throws Exception {
        StopTransactionRequest request = objectMapper.treeToValue(payload, StopTransactionRequest.class);

        logger.info("StopTransaction de {} - Transaction {}, Reason: {}",
                ocppId, request.transactionId(), request.reason());

        // 1. Buscar SessionId por transactionId
        SessionId sessionId = transactionMappingService.findSessionIdByTransactionId(request.transactionId())
                .orElseThrow(() -> new IllegalStateException(
                        "Session not found for transactionId: " + request.transactionId()));

        // 2. Completar sesión de carga
        try {
            BigDecimal finalEnergy = BigDecimal.valueOf(request.meterStop());

            StopChargingSessionUseCase.StopSessionCommand command = new StopChargingSessionUseCase.StopSessionCommand(
                    sessionId,
                    finalEnergy,
                    request.reason());

            stopSessionUseCase.execute(command);

            // 3. Limpiar mapeo
            transactionMappingService.removeMapping(request.transactionId());

            logger.info("Sesión completada: sessionId={}, transactionId={}", sessionId, request.transactionId());

            return new StopTransactionResponse(
                    new IdTagInfo("Accepted", null, null));
        } catch (Exception e) {
            logger.error("Error completando sesión", e);
            return new StopTransactionResponse(
                    new IdTagInfo("Invalid", null, null));
        }
    }

    // ========== Métodos auxiliares ==========

    private ConnectorStatus mapOcppStatusToDomain(String ocppStatus) {
        return switch (ocppStatus) {
            case "Available" -> ConnectorStatus.AVAILABLE;
            case "Preparing" -> ConnectorStatus.PREPARING;
            case "Charging" -> ConnectorStatus.CHARGING;
            case "SuspendedEVSE", "SuspendedEV" -> ConnectorStatus.SUSPENDED_EV;
            case "Finishing" -> ConnectorStatus.FINISHING;
            case "Reserved" -> ConnectorStatus.RESERVED;
            case "Unavailable" -> ConnectorStatus.UNAVAILABLE;
            case "Faulted" -> ConnectorStatus.FAULTED;
            default -> ConnectorStatus.UNAVAILABLE;
        };
    }

    private BigDecimal extractEnergyFromMeterValues(MeterValuesRequest request) {
        for (MeterValue meterValue : request.meterValue()) {
            for (SampledValue sample : meterValue.sampledValue()) {
                if ("Energy.Active.Import.Register".equals(sample.measurand())) {
                    String value = sample.value();
                    String unit = sample.unit();

                    BigDecimal energy = new BigDecimal(value);

                    // Convertir a kWh si es necesario
                    if ("Wh".equals(unit)) {
                        energy = energy.divide(new BigDecimal("1000"));
                    }

                    return energy;
                }
            }
        }
        return null;
    }

    private int generateTransactionId() {
        // En producción, usar secuencia de BD o UUID
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    /**
     * Maneja mensajes Authorize (validación de idTag).
     */
    private AuthorizeResponse handleAuthorize(JsonNode payload, String ocppId) throws Exception {
        AuthorizeRequest request = objectMapper.treeToValue(payload, AuthorizeRequest.class);

        logger.info("Authorize de {} - IdTag: {}", ocppId, request.idTag());

        // Validar autorización RFID
        var authInfo = rfidAuthService.authorize(request.idTag());

        return new AuthorizeResponse(authInfo);
    }

    private DiagnosticsStatusNotificationResponse handleDiagnosticsStatusNotification(JsonNode payload, String ocppId)
            throws Exception {
        DiagnosticsStatusNotificationRequest request = objectMapper.treeToValue(payload,
                DiagnosticsStatusNotificationRequest.class);

        logger.info("DiagnosticsStatusNotification de {}: {}", ocppId, request.status());

        // TODO: Almacenar estado de diagnóstico en BD o notificar vía evento

        return new DiagnosticsStatusNotificationResponse();
    }

    private FirmwareStatusNotificationResponse handleFirmwareStatusNotification(JsonNode payload, String ocppId)
            throws Exception {
        FirmwareStatusNotificationRequest request = objectMapper.treeToValue(payload,
                FirmwareStatusNotificationRequest.class);

        logger.info("FirmwareStatusNotification de {}: {}", ocppId, request.status());

        // TODO: Almacenar estado de firmware en BD o notificar vía evento

        return new FirmwareStatusNotificationResponse();
    }
}
