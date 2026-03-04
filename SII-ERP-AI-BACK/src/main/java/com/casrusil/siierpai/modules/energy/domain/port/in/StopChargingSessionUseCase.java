package com.casrusil.siierpai.modules.energy.domain.port.in;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.math.BigDecimal;

/**
 * Puerto de Entrada: Caso de Uso - Detener Sesión de Carga.
 * 
 * <h2>Propósito de Negocio</h2>
 * <p>
 * Permite finalizar una sesión de carga activa, calculando el consumo total de
 * energía
 * y generando automáticamente el asiento contable correspondiente. Este es el
 * caso de uso
 * más crítico para la integración financiera del sistema.
 * 
 * <h2>Flujo de Negocio</h2>
 * <ol>
 * <li>Valida que la sesión existe y está activa</li>
 * <li>Registra la medición final de energía</li>
 * <li>Calcula el consumo total (energía final - energía inicial)</li>
 * <li>Calcula el costo usando el servicio de pricing dinámico</li>
 * <li>Actualiza el estado del conector a "Available"</li>
 * <li>Emite {@code ChargingSessionCompletedEvent} con todos los datos
 * financieros</li>
 * </ol>
 * 
 * <h2>Integración con Contabilidad (CRÍTICO)</h2>
 * <p>
 * El evento {@code ChargingSessionCompletedEvent} dispara automáticamente la
 * creación
 * de un asiento contable balanceado en el módulo de contabilidad:
 * 
 * <pre>
 * DEBE:  Clientes Carga (1.1.05.002)      - Total a cobrar
 * HABER: Ingresos por Carga (4.1.02.001)  - Reconocimiento de ingreso
 * DEBE:  Costo de Energía (5.1.02.001)    - Costo de la energía consumida
 * HABER: Proveedores Energía (2.1.02.001) - Deuda con proveedor eléctrico
 * </pre>
 * 
 * <p>
 * Este procesamiento es <b>asíncrono</b> (usando Virtual Threads) para no
 * bloquear
 * la respuesta al cargador OCPP.
 * 
 * <h2>Arquitectura</h2>
 * <ul>
 * <li><b>Patrón:</b> Hexagonal Architecture - Input Port (Use Case)</li>
 * <li><b>Implementación:</b> {@code ChargingSessionService} en
 * application/service</li>
 * <li><b>Protocolo:</b> OCPP 1.6/2.0 - StopTransaction</li>
 * <li><b>Event-Driven:</b> Desacoplamiento total entre Energy y Accounting</li>
 * </ul>
 * 
 * @see InitiateChargingSessionUseCase
 * @see com.casrusil.siierpai.modules.accounting.application.listener.ChargingEventListener
 * @since 1.0
 */
public interface StopChargingSessionUseCase {

    /**
     * Detiene una sesión de carga activa y genera el asiento contable
     * automáticamente.
     * 
     * <p>
     * Este método completa la sesión de carga, calcula el consumo total y el costo,
     * y emite un evento que dispara la creación automática del asiento contable.
     * 
     * <h3>Eventos Emitidos</h3>
     * <ul>
     * <li>{@code ChargingSessionCompletedEvent} - Contiene todos los datos para
     * contabilidad</li>
     * </ul>
     * 
     * <h3>Efectos Secundarios</h3>
     * <ul>
     * <li>Actualiza el estado del conector a "Available"</li>
     * <li>Persiste la sesión completada en la base de datos</li>
     * <li><b>Crea asiento contable automáticamente</b> (vía evento asíncrono)</li>
     * <li>Actualiza métricas de sostenibilidad (CO2 ahorrado)</li>
     * </ul>
     * 
     * @param command Comando con los datos de finalización (sessionId, energía
     *                final, razón de parada)
     * @return La sesión de carga completada con todos los cálculos financieros
     * @throws IllegalArgumentException si la sesión no existe
     * @throws IllegalStateException    si la sesión no está activa
     */
    ChargingSession execute(StopSessionCommand command);

    /**
     * Comando para detener una sesión de carga.
     */
    record StopSessionCommand(
            SessionId sessionId,
            BigDecimal finalEnergyKwh,
            String stopReason) {
        public StopSessionCommand {
            if (sessionId == null) {
                throw new IllegalArgumentException("SessionId es obligatorio");
            }
            if (finalEnergyKwh == null || finalEnergyKwh.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("La energía final no puede ser negativa");
            }
        }
    }
}
