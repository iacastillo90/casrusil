package com.casrusil.siierpai.modules.energy.domain.event;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Evento de dominio publicado cuando un cargador OCPP reporta el estado de una actualización de firmware.
 *
 * <p>Este evento es capturado por {@code OcppFirmwareStatusEventListener} para:
 * <ul>
 *   <li>Actualizar el campo {@code firmwareVersion} en la entidad {@code ChargingStation}</li>
 *   <li>Registrar el historial de actualizaciones de firmware</li>
 *   <li>Alertar al equipo de operaciones si el estado es "InstallationFailed"</li>
 * </ul>
 *
 * <p>Estados OCPP de firmware posibles:
 * <ul>
 *   <li>{@code Downloaded} - Descarga completada</li>
 *   <li>{@code DownloadFailed} - Error en descarga</li>
 *   <li>{@code Installing} - Instalando</li>
 *   <li>{@code Installed} - Instalado correctamente</li>
 *   <li>{@code InstallationFailed} - Error en instalación</li>
 * </ul>
 *
 * @see OcppDiagnosticsEvent
 * @since 1.0
 */
public class OcppFirmwareStatusEvent extends ApplicationEvent {

    private final String ocppId;
    private final String status;
    private final Instant timestamp;

    public OcppFirmwareStatusEvent(Object source, String ocppId, String status, Instant timestamp) {
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
