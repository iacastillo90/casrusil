package com.casrusil.siierpai.modules.energy.application.service;

import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.firmware.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.parser.OcppMessageParser;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.session.OcppSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Servicio de Aplicación: Comandos OCPP Remotos (Server → Charger).
 * 
 * Permite enviar comandos a cargadores y esperar sus respuestas de forma
 * asíncrona.
 * Usa CompletableFuture para manejar la naturaleza asíncrona de OCPP WebSocket.
 * 
 * Patrón: Application Service
 */
@Service
public class OcppCommandService {

        private static final Logger logger = LoggerFactory.getLogger(OcppCommandService.class);
        private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

        private final OcppSessionManager sessionManager;
        private final OcppMessageParser messageParser;
        private final ObjectMapper objectMapper;

        // Mapa de comandos pendientes: uniqueId → CompletableFuture<Response>
        private final Map<String, CompletableFuture<Object>> pendingCommands = new ConcurrentHashMap<>();

        public OcppCommandService(
                        OcppSessionManager sessionManager,
                        OcppMessageParser messageParser,
                        ObjectMapper objectMapper) {
                this.sessionManager = sessionManager;
                this.messageParser = messageParser;
                this.objectMapper = objectMapper;
        }

        /**
         * Inicia una transacción de carga remotamente.
         */
        public CompletableFuture<RemoteStartTransactionResponse> remoteStartTransaction(
                        String ocppId,
                        int connectorId,
                        String idTag) {
                RemoteStartTransactionRequest request = new RemoteStartTransactionRequest(connectorId, idTag);

                return sendCommand(
                                ocppId,
                                "RemoteStartTransaction",
                                request,
                                RemoteStartTransactionResponse.class);
        }

        /**
         * Detiene una transacción de carga remotamente.
         */
        public CompletableFuture<RemoteStopTransactionResponse> remoteStopTransaction(
                        String ocppId,
                        int transactionId) {
                RemoteStopTransactionRequest request = new RemoteStopTransactionRequest(transactionId);

                return sendCommand(
                                ocppId,
                                "RemoteStopTransaction",
                                request,
                                RemoteStopTransactionResponse.class);
        }

        /**
         * Desbloquea un conector físicamente.
         */
        public CompletableFuture<UnlockConnectorResponse> unlockConnector(
                        String ocppId,
                        int connectorId) {
                UnlockConnectorRequest request = new UnlockConnectorRequest(connectorId);

                return sendCommand(
                                ocppId,
                                "UnlockConnector",
                                request,
                                UnlockConnectorResponse.class);
        }

        /**
         * Reinicia un cargador (Soft o Hard).
         */
        public CompletableFuture<ResetResponse> reset(
                        String ocppId,
                        String resetType // "Soft" | "Hard"
        ) {
                ResetRequest request = new ResetRequest(resetType);

                return sendCommand(
                                ocppId,
                                "Reset",
                                request,
                                ResetResponse.class);
        }

        /**
         * Cambia un parámetro de configuración del cargador.
         */
        public CompletableFuture<ChangeConfigurationResponse> changeConfiguration(
                        String ocppId,
                        String key,
                        String value) {
                ChangeConfigurationRequest request = new ChangeConfigurationRequest(key, value);

                return sendCommand(
                                ocppId,
                                "ChangeConfiguration",
                                request,
                                ChangeConfigurationResponse.class);
        }

        /**
         * Obtiene la configuración actual del cargador.
         */
        public CompletableFuture<GetConfigurationResponse> getConfiguration(
                        String ocppId,
                        List<String> keys) {
                GetConfigurationRequest request = new GetConfigurationRequest(keys);

                return sendCommand(
                                ocppId,
                                "GetConfiguration",
                                request,
                                GetConfigurationResponse.class);
        }

        /**
         * Cambia la disponibilidad de un conector o estación completa.
         */
        public CompletableFuture<ChangeAvailabilityResponse> changeAvailability(
                        String ocppId,
                        int connectorId, // 0 = toda la estación
                        String type // "Operative" | "Inoperative"
        ) {
                ChangeAvailabilityRequest request = new ChangeAvailabilityRequest(connectorId, type);

                return sendCommand(
                                ocppId,
                                "ChangeAvailability",
                                request,
                                ChangeAvailabilityResponse.class);
        }

        /**
         * Establece un perfil de carga para Smart Charging.
         */
        public CompletableFuture<SetChargingProfileResponse> setChargingProfile(
                        String ocppId,
                        int connectorId,
                        ChargingProfile chargingProfile) {
                SetChargingProfileRequest request = new SetChargingProfileRequest(connectorId, chargingProfile);

                return sendCommand(
                                ocppId,
                                "SetChargingProfile",
                                request,
                                SetChargingProfileResponse.class);
        }

        /**
         * Elimina perfiles de carga.
         */
        public CompletableFuture<ClearChargingProfileResponse> clearChargingProfile(
                        String ocppId,
                        Integer id,
                        Integer connectorId,
                        String chargingProfilePurpose,
                        Integer stackLevel) {
                ClearChargingProfileRequest request = new ClearChargingProfileRequest(
                                id, connectorId, chargingProfilePurpose, stackLevel);

                return sendCommand(
                                ocppId,
                                "ClearChargingProfile",
                                request,
                                ClearChargingProfileResponse.class);
        }

        /**
         * Obtiene el horario de carga calculado.
         */
        public CompletableFuture<GetCompositeScheduleResponse> getCompositeSchedule(
                        String ocppId,
                        int connectorId,
                        int duration,
                        String chargingRateUnit) {
                GetCompositeScheduleRequest request = new GetCompositeScheduleRequest(
                                connectorId, duration, chargingRateUnit);

                return sendCommand(
                                ocppId,
                                "GetCompositeSchedule",
                                request,
                                GetCompositeScheduleResponse.class);
        }

        /**
         * Solicita logs de diagnóstico del cargador.
         */
        public CompletableFuture<GetDiagnosticsResponse> getDiagnostics(
                        String ocppId,
                        String location,
                        String startTime,
                        String stopTime) {
                GetDiagnosticsRequest request = new GetDiagnosticsRequest(
                                location, null, null, startTime, stopTime);

                return sendCommand(
                                ocppId,
                                "GetDiagnostics",
                                request,
                                GetDiagnosticsResponse.class);
        }

        /**
         * Solicita actualización de firmware OTA.
         */
        public CompletableFuture<UpdateFirmwareResponse> updateFirmware(
                        String ocppId,
                        String location,
                        String retrieveDate) {
                UpdateFirmwareRequest request = new UpdateFirmwareRequest(
                                location, retrieveDate, null, null);

                return sendCommand(
                                ocppId,
                                "UpdateFirmware",
                                request,
                                UpdateFirmwareResponse.class);
        }

        /**
         * Reserva un conector para un usuario específico.
         */
        public CompletableFuture<ReserveNowResponse> reserveNow(
                        String ocppId,
                        int connectorId,
                        String expiryDate,
                        String idTag,
                        int reservationId) {
                ReserveNowRequest request = new ReserveNowRequest(
                                connectorId, expiryDate, idTag, reservationId, null);

                return sendCommand(
                                ocppId,
                                "ReserveNow",
                                request,
                                ReserveNowResponse.class);
        }

        /**
         * Cancela una reserva.
         */
        public CompletableFuture<CancelReservationResponse> cancelReservation(
                        String ocppId,
                        int reservationId) {
                CancelReservationRequest request = new CancelReservationRequest(reservationId);

                return sendCommand(
                                ocppId,
                                "CancelReservation",
                                request,
                                CancelReservationResponse.class);
        }

        /**
         * Solicita al cargador que envíe un mensaje específico.
         */
        public CompletableFuture<TriggerMessageResponse> triggerMessage(
                        String ocppId,
                        String requestedMessage,
                        Integer connectorId) {
                TriggerMessageRequest request = new TriggerMessageRequest(requestedMessage, connectorId);

                return sendCommand(
                                ocppId,
                                "TriggerMessage",
                                request,
                                TriggerMessageResponse.class);
        }

        /**
         * Limpia la caché de autorización del cargador.
         */
        public CompletableFuture<ClearCacheResponse> clearCache(String ocppId) {
                ClearCacheRequest request = new ClearCacheRequest();

                return sendCommand(
                                ocppId,
                                "ClearCache",
                                request,
                                ClearCacheResponse.class);
        }

        /**
         * Envía un comando OCPP y retorna un CompletableFuture con la respuesta.
         */
        private <T> CompletableFuture<T> sendCommand(
                        String ocppId,
                        String action,
                        Object request,
                        Class<T> responseClass) {
                // 1. Validar que el cargador está conectado
                if (!sessionManager.isConnected(ocppId)) {
                        CompletableFuture<T> future = new CompletableFuture<>();
                        future.completeExceptionally(new IllegalStateException("Cargador no conectado: " + ocppId));
                        return future;
                }

                // 2. Generar uniqueId
                String uniqueId = UUID.randomUUID().toString();

                // 3. Crear CompletableFuture para la respuesta
                CompletableFuture<Object> responseFuture = new CompletableFuture<>();
                pendingCommands.put(uniqueId, responseFuture);

                try {
                        // 4. Serializar comando OCPP CALL: [2, uniqueId, action, payload]
                        String message = messageParser.serializeCall(uniqueId, action, request);

                        // 5. Enviar mensaje al cargador
                        sessionManager.sendMessage(ocppId, message);

                        logger.info("Comando enviado a {}: {} (uniqueId: {})", ocppId, action, uniqueId);

                        // 6. Configurar timeout
                        CompletableFuture<T> typedFuture = responseFuture
                                        .orTimeout(DEFAULT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                                        .thenApply(response -> objectMapper.convertValue(response, responseClass))
                                        .whenComplete((result, error) -> {
                                                // Limpiar comando pendiente
                                                pendingCommands.remove(uniqueId);

                                                if (error != null) {
                                                        if (error instanceof TimeoutException) {
                                                                logger.error("Timeout esperando respuesta de {} para {}",
                                                                                ocppId, action);
                                                        } else {
                                                                logger.error("Error procesando respuesta de {}", ocppId,
                                                                                error);
                                                        }
                                                }
                                        });

                        return typedFuture;

                } catch (Exception e) {
                        pendingCommands.remove(uniqueId);
                        CompletableFuture<T> future = new CompletableFuture<>();
                        future.completeExceptionally(e);
                        return future;
                }
        }

        /**
         * Completa un CompletableFuture cuando llega la respuesta del cargador.
         * Este método es llamado por OcppWebSocketHandler al recibir un CALLRESULT.
         */
        public void completeCommand(String uniqueId, Object response) {
                CompletableFuture<Object> future = pendingCommands.get(uniqueId);

                if (future != null) {
                        future.complete(response);
                        logger.debug("Comando completado: {}", uniqueId);
                } else {
                        logger.warn("Respuesta recibida para comando desconocido: {}", uniqueId);
                }
        }

        /**
         * Completa un CompletableFuture con error.
         */
        public void failCommand(String uniqueId, String errorCode, String errorDescription) {
                CompletableFuture<Object> future = pendingCommands.get(uniqueId);

                if (future != null) {
                        future.completeExceptionally(
                                        new RuntimeException(String.format("OCPP Error %s: %s", errorCode,
                                                        errorDescription)));
                        pendingCommands.remove(uniqueId);
                        logger.error("Comando fallido: {} - {} {}", uniqueId, errorCode, errorDescription);
                }
        }
}
