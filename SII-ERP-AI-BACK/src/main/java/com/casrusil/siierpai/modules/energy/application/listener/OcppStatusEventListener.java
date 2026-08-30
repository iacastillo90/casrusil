package com.casrusil.siierpai.modules.energy.application.listener;

import com.casrusil.siierpai.modules.energy.domain.event.OcppDiagnosticsEvent;
import com.casrusil.siierpai.modules.energy.domain.event.OcppFirmwareStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener que persiste los eventos de estado OCPP (diagnóstico y firmware).
 *
 * <p>Recibe eventos publicados por {@code OcppMessageDispatcher} y los procesa de forma
 * asíncrona para no bloquear el ciclo de respuesta del protocolo WebSocket.
 *
 * <p>La persistencia real en BD (tabla {@code ocpp_event_log}) está preparada para ser
 * completada cuando se cree la entidad {@code OcppEventLogEntity} y su repositorio JPA.
 * Por ahora, el log de nivel INFO garantiza trazabilidad completa en producción.
 *
 * @since 1.0
 */
@Component
public class OcppStatusEventListener {

    private static final Logger log = LoggerFactory.getLogger(OcppStatusEventListener.class);

    /**
     * Procesa notificaciones de diagnóstico OCPP.
     * Persiste el resultado en auditoría (actualmente via log estructurado).
     * TODO: Persistir en tabla ocpp_diagnostics_audit cuando esté el repositorio JPA.
     */
    @Async
    @EventListener
    public void onDiagnosticsStatus(OcppDiagnosticsEvent event) {
        log.info("[OCPP-AUDIT] DiagnosticsStatus | stationId={} | status={} | timestamp={}",
                event.getOcppId(), event.getStatus(), event.getTimestamp());

        // Alertar si el diagnóstico falló
        if ("UploadFailed".equalsIgnoreCase(event.getStatus())) {
            log.warn("[OCPP-ALERT] Error subiendo diagnóstico del cargador {}. " +
                    "Verificar conectividad del servidor FTP/SFTP de diagnósticos.", event.getOcppId());
        }
    }

    /**
     * Procesa notificaciones de estado de firmware OCPP.
     * Actualiza el estado del cargador según el resultado de la instalación.
     * TODO: Persistir firmwareVersion en la entidad ChargingStation via repositorio JPA.
     */
    @Async
    @EventListener
    public void onFirmwareStatus(OcppFirmwareStatusEvent event) {
        log.info("[OCPP-AUDIT] FirmwareStatus | stationId={} | status={} | timestamp={}",
                event.getOcppId(), event.getStatus(), event.getTimestamp());

        // Alertar si la instalación falló
        if ("InstallationFailed".equalsIgnoreCase(event.getStatus()) ||
                "DownloadFailed".equalsIgnoreCase(event.getStatus())) {
            log.error("[OCPP-ALERT] Fallo en actualización de firmware del cargador {}. " +
                    "Status: {}. Revisar logs del cargador e intentar rollback.", event.getOcppId(), event.getStatus());
        }

        // Registrar instalación exitosa
        if ("Installed".equalsIgnoreCase(event.getStatus())) {
            log.info("[OCPP-INFO] Firmware actualizado exitosamente en cargador {}.", event.getOcppId());
        }
    }
}
