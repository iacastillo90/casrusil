package com.casrusil.siierpai.modules.energy.domain.event;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Evento de dominio publicado cuando un cargador OCPP reporta el estado de un proceso de diagnóstico.
 *
 * <p>Este evento es capturado por {@code OcppDiagnosticsEventListener} para:
 * <ul>
 *   <li>Persistir el estado en la tabla {@code ocpp_diagnostics_audit}</li>
 *   <li>Notificar al equipo de operaciones si el estado es "Uploaded" o "UploadFailed"</li>
 * </ul>
 *
 * @see OcppFirmwareStatusEvent
 * @since 1.0
 */
public class OcppDiagnosticsEvent extends ApplicationEvent {

    private final String ocppId;
    private final String status;
    private final Instant timestamp;

    public OcppDiagnosticsEvent(Object source, String ocppId, String status, Instant timestamp) {
        super(source);
        this.ocppId = ocppId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getOcppId() {
        return ocppId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
