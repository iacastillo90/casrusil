package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.controller;

import com.casrusil.siierpai.modules.energy.application.service.OcppCommandService;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.firmware.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller: Comandos Remotos OCPP.
 * 
 * Permite enviar comandos a cargadores desde el backend.
 * Endpoints para control remoto de estaciones de carga.
 */
@RestController
@RequestMapping("/api/v1/ocpp/remote")
public class OcppRemoteController {

        private final OcppCommandService commandService;

        public OcppRemoteController(OcppCommandService commandService) {
                this.commandService = commandService;
        }

        /**
         * POST /api/v1/ocpp/remote/start
         * Inicia una transacción de carga remotamente.
         */
        @PostMapping("/start")
        public CompletableFuture<ResponseEntity<RemoteStartTransactionResponse>> remoteStart(
                        @RequestParam String ocppId,
                        @RequestParam int connectorId,
                        @RequestParam String idTag) {
                return commandService.remoteStartTransaction(ocppId, connectorId, idTag)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/remote/stop
         * Detiene una transacción de carga remotamente.
         */
        @PostMapping("/stop")
        public CompletableFuture<ResponseEntity<RemoteStopTransactionResponse>> remoteStop(
                        @RequestParam String ocppId,
                        @RequestParam int transactionId) {
                return commandService.remoteStopTransaction(ocppId, transactionId)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/remote/unlock
         * Desbloquea un conector físicamente.
         */
        @PostMapping("/unlock")
        public CompletableFuture<ResponseEntity<UnlockConnectorResponse>> unlockConnector(
                        @RequestParam String ocppId,
                        @RequestParam int connectorId) {
                return commandService.unlockConnector(ocppId, connectorId)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/remote/reset
         * Reinicia un cargador (Soft o Hard).
         */
        @PostMapping("/reset")
        public CompletableFuture<ResponseEntity<ResetResponse>> reset(
                        @RequestParam String ocppId,
                        @RequestParam(defaultValue = "Soft") String resetType) {
                return commandService.reset(ocppId, resetType)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/config/change
         * Cambia un parámetro de configuración del cargador.
         */
        @PostMapping("/config/change")
        public CompletableFuture<ResponseEntity<ChangeConfigurationResponse>> changeConfiguration(
                        @RequestParam String ocppId,
                        @RequestParam String key,
                        @RequestParam String value) {
                return commandService.changeConfiguration(ocppId, key, value)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * GET /api/v1/ocpp/config/get
         * Obtiene la configuración actual del cargador.
         */
        @GetMapping("/config/get")
        public CompletableFuture<ResponseEntity<GetConfigurationResponse>> getConfiguration(
                        @RequestParam String ocppId,
                        @RequestParam(required = false) List<String> keys) {
                return commandService.getConfiguration(ocppId, keys != null ? keys : List.of())
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/availability/change
         * Cambia la disponibilidad de un conector.
         */
        @PostMapping("/availability/change")
        public CompletableFuture<ResponseEntity<ChangeAvailabilityResponse>> changeAvailability(
                        @RequestParam String ocppId,
                        @RequestParam int connectorId,
                        @RequestParam String type) {
                return commandService.changeAvailability(ocppId, connectorId, type)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/charging-profile/set
         * Establece un perfil de carga para Smart Charging.
         */
        @PostMapping("/charging-profile/set")
        public CompletableFuture<ResponseEntity<SetChargingProfileResponse>> setChargingProfile(
                        @RequestParam String ocppId,
                        @RequestParam int connectorId,
                        @RequestBody ChargingProfile chargingProfile) {
                return commandService.setChargingProfile(ocppId, connectorId, chargingProfile)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/charging-profile/clear
         * Elimina perfiles de carga.
         */
        @PostMapping("/charging-profile/clear")
        public CompletableFuture<ResponseEntity<ClearChargingProfileResponse>> clearChargingProfile(
                        @RequestParam String ocppId,
                        @RequestParam(required = false) Integer id,
                        @RequestParam(required = false) Integer connectorId,
                        @RequestParam(required = false) String chargingProfilePurpose,
                        @RequestParam(required = false) Integer stackLevel) {
                return commandService.clearChargingProfile(ocppId, id, connectorId, chargingProfilePurpose, stackLevel)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * GET /api/v1/ocpp/charging-profile/schedule
         * Obtiene el horario de carga calculado.
         */
        @GetMapping("/charging-profile/schedule")
        public CompletableFuture<ResponseEntity<GetCompositeScheduleResponse>> getCompositeSchedule(
                        @RequestParam String ocppId,
                        @RequestParam int connectorId,
                        @RequestParam int duration,
                        @RequestParam(defaultValue = "W") String chargingRateUnit) {
                return commandService.getCompositeSchedule(ocppId, connectorId, duration, chargingRateUnit)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/diagnostics/get
         * Solicita logs de diagnóstico del cargador.
         */
        @PostMapping("/diagnostics/get")
        public CompletableFuture<ResponseEntity<GetDiagnosticsResponse>> getDiagnostics(
                        @RequestParam String ocppId,
                        @RequestParam String location,
                        @RequestParam(required = false) String startTime,
                        @RequestParam(required = false) String stopTime) {
                return commandService.getDiagnostics(ocppId, location, startTime, stopTime)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/firmware/update
         * Solicita actualización de firmware OTA.
         */
        @PostMapping("/firmware/update")
        public CompletableFuture<ResponseEntity<UpdateFirmwareResponse>> updateFirmware(
                        @RequestParam String ocppId,
                        @RequestParam String location,
                        @RequestParam String retrieveDate) {
                return commandService.updateFirmware(ocppId, location, retrieveDate)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/reservation/reserve
         * Reserva un conector para un usuario.
         */
        @PostMapping("/reservation/reserve")
        public CompletableFuture<ResponseEntity<ReserveNowResponse>> reserveNow(
                        @RequestParam String ocppId,
                        @RequestParam int connectorId,
                        @RequestParam String expiryDate,
                        @RequestParam String idTag,
                        @RequestParam int reservationId) {
                return commandService.reserveNow(ocppId, connectorId, expiryDate, idTag, reservationId)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/reservation/cancel
         * Cancela una reserva.
         */
        @PostMapping("/reservation/cancel")
        public CompletableFuture<ResponseEntity<CancelReservationResponse>> cancelReservation(
                        @RequestParam String ocppId,
                        @RequestParam int reservationId) {
                return commandService.cancelReservation(ocppId, reservationId)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/trigger-message
         * Solicita al cargador que envíe un mensaje específico.
         */
        @PostMapping("/trigger-message")
        public CompletableFuture<ResponseEntity<TriggerMessageResponse>> triggerMessage(
                        @RequestParam String ocppId,
                        @RequestParam String requestedMessage,
                        @RequestParam(required = false) Integer connectorId) {
                return commandService.triggerMessage(ocppId, requestedMessage, connectorId)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }

        /**
         * POST /api/v1/ocpp/clear-cache
         * Limpia la caché de autorización del cargador.
         */
        @PostMapping("/clear-cache")
        public CompletableFuture<ResponseEntity<ClearCacheResponse>> clearCache(
                        @RequestParam String ocppId) {
                return commandService.clearCache(ocppId)
                                .thenApply(ResponseEntity::ok)
                                .exceptionally(ex -> ResponseEntity.internalServerError().build());
        }
}
