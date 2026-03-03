package com.casrusil.siierpai.modules.energy.domain.port.in;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.math.BigDecimal;

/**
 * Puerto de Entrada: Caso de Uso - Registrar Consumo de Energía.
 * 
 * <h2>Propósito de Negocio</h2>
 * <p>
 * Permite registrar mediciones periódicas de energía durante una sesión activa,
 * proporcionando visibilidad en tiempo real del consumo. Estas mediciones son
 * enviadas
 * automáticamente por los cargadores OCPP (mensaje MeterValues) cada cierto
 * intervalo
 * (típicamente cada 60 segundos).
 * 
 * <h2>Flujo de Negocio</h2>
 * <ol>
 * <li>Valida que la sesión existe y está activa</li>
 * <li>Registra la medición de energía con timestamp</li>
 * <li>Calcula el consumo acumulado desde el inicio</li>
 * <li>Emite {@code EnergyConsumedEvent} para tracking en tiempo real</li>
 * </ol>
 * 
 * <h2>Integración con Otros Módulos</h2>
 * <p>
 * El evento {@code EnergyConsumedEvent} puede ser usado por:
 * <ul>
 * <li><b>Dashboard en tiempo real:</b> Mostrar consumo actual al usuario</li>
 * <li><b>Alertas:</b> Notificar si el consumo excede límites</li>
 * <li><b>Pricing dinámico:</b> Ajustar tarifas según demanda</li>
 * <li><b>Contabilidad (futuro):</b> Reconocimiento de ingresos progresivo</li>
 * </ul>
 * 
 * <p>
 * <b>Nota:</b> Actualmente, el asiento contable se genera solo al finalizar la
 * sesión
 * (ver {@link StopChargingSessionUseCase}). En el futuro, se podría implementar
 * reconocimiento de ingresos progresivo basado en estas mediciones.
 * 
 * <h2>Arquitectura</h2>
 * <ul>
 * <li><b>Patrón:</b> Hexagonal Architecture - Input Port (Use Case)</li>
 * <li><b>Implementación:</b> {@code ChargingSessionService} en
 * application/service</li>
 * <li><b>Protocolo:</b> OCPP 1.6/2.0 - MeterValues</li>
 * <li><b>Frecuencia:</b> Típicamente cada 60 segundos durante la carga</li>
 * </ul>
 * 
 * @see InitiateChargingSessionUseCase
 * @see StopChargingSessionUseCase
 * @since 1.0
 */
public interface RecordEnergyConsumptionUseCase {

    /**
     * Registra una medición de energía durante una sesión activa.
     * 
     * <p>
     * Este método es llamado periódicamente por el adaptador OCPP cuando el
     * cargador
     * envía mensajes MeterValues. Permite tracking en tiempo real del consumo de
     * energía.
     * 
     * <h3>Eventos Emitidos</h3>
     * <ul>
     * <li>{@code EnergyConsumedEvent} - Contiene la medición y el consumo
     * acumulado</li>
     * </ul>
     * 
     * <h3>Efectos Secundarios</h3>
     * <ul>
     * <li>Actualiza el historial de mediciones de la sesión</li>
     * <li>Persiste la medición en la base de datos</li>
     * <li>Notifica a listeners para dashboard en tiempo real</li>
     * </ul>
     * 
     * @param command Comando con los datos de la medición (sessionId, energía,
     *                valor del medidor)
     * @throws IllegalArgumentException si la sesión no existe
     * @throws IllegalStateException    si la sesión no está activa
     */
    void execute(RecordEnergyCommand command);

    /**
     * Comando para registrar consumo de energía.
     */
    record RecordEnergyCommand(
            SessionId sessionId,
            BigDecimal energyKwh,
            String meterValue) {
        public RecordEnergyCommand {
            if (sessionId == null) {
                throw new IllegalArgumentException("SessionId es obligatorio");
            }
            if (energyKwh == null || energyKwh.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("La energía no puede ser negativa");
            }
        }
    }
}
