package com.casrusil.siierpai.modules.energy.domain.port.in;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de Entrada: Caso de Uso - Iniciar Sesión de Carga.
 * 
 * <h2>Propósito de Negocio</h2>
 * <p>
 * Permite a un usuario autorizado iniciar una sesión de carga en un conector
 * específico
 * de una estación de carga. Este caso de uso es el punto de entrada principal
 * para el
 * protocolo OCPP (Open Charge Point Protocol) cuando un vehículo eléctrico se
 * conecta
 * a un cargador.
 * 
 * <h2>Flujo de Negocio</h2>
 * <ol>
 * <li>Valida que la estación y el conector existen</li>
 * <li>Verifica que el conector está disponible para carga</li>
 * <li>Confirma que no hay sesión activa en el conector</li>
 * <li>Crea la sesión de carga con medición inicial</li>
 * <li>Actualiza el estado del conector a "Charging"</li>
 * <li>Emite {@code ChargingSessionStartedEvent} para notificar a otros
 * módulos</li>
 * </ol>
 * 
 * <h2>Integración con Contabilidad</h2>
 * <p>
 * El evento {@code ChargingSessionStartedEvent} es escuchado por el módulo de
 * contabilidad
 * para preparar el registro de ingresos. El asiento contable final se crea
 * cuando la
 * sesión se completa (ver {@link StopChargingSessionUseCase}).
 * 
 * <h2>Arquitectura</h2>
 * <ul>
 * <li><b>Patrón:</b> Hexagonal Architecture - Input Port (Use Case)</li>
 * <li><b>Implementación:</b> {@code ChargingSessionService} en
 * application/service</li>
 * <li><b>Protocolo:</b> OCPP 1.6/2.0 - StartTransaction</li>
 * </ul>
 * 
 * @see StopChargingSessionUseCase
 * @see RecordEnergyConsumptionUseCase
 * @since 1.0
 */
public interface InitiateChargingSessionUseCase {

    /**
     * Inicia una nueva sesión de carga en un conector específico.
     * 
     * <p>
     * Este método valida todos los requisitos previos (estación existe, conector
     * disponible,
     * sin sesión activa) antes de crear la sesión. Si alguna validación falla,
     * lanza una
     * excepción apropiada.
     * 
     * <h3>Eventos Emitidos</h3>
     * <ul>
     * <li>{@code ChargingSessionStartedEvent} - Notifica el inicio de la
     * sesión</li>
     * </ul>
     * 
     * <h3>Efectos Secundarios</h3>
     * <ul>
     * <li>Actualiza el estado del conector a "Charging"</li>
     * <li>Persiste la nueva sesión en la base de datos</li>
     * <li>Notifica al módulo de contabilidad (vía evento)</li>
     * </ul>
     * 
     * @param command Comando con los datos de la sesión (estación, conector,
     *                usuario, energía inicial)
     * @return La sesión de carga recién creada con estado ACTIVE
     * @throws IllegalArgumentException si la estación o conector no existen
     * @throws IllegalStateException    si el conector no está disponible o ya tiene
     *                                  sesión activa
     */
    ChargingSession execute(InitiateSessionCommand command);

    /**
     * Comando para iniciar una sesión de carga.
     */
    record InitiateSessionCommand(
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId,
            UUID userId,
            BigDecimal initialEnergyKwh) {
        public InitiateSessionCommand {
            if (stationId == null) {
                throw new IllegalArgumentException("StationId es obligatorio");
            }
            if (chargerOcppId == null) {
                throw new IllegalArgumentException("ChargerOcppId es obligatorio");
            }
            if (connectorId == null) {
                throw new IllegalArgumentException("ConnectorId es obligatorio");
            }
            if (userId == null) {
                throw new IllegalArgumentException("UserId es obligatorio");
            }
            if (initialEnergyKwh == null || initialEnergyKwh.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("La energía inicial no puede ser negativa");
            }
        }
    }
}
