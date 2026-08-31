package com.casrusil.siierpai.modules.energy.infrastructure.adapter.out;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingStationControlPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptador de Infraestructura: Control de Estaciones de Carga vía OCPP.
 * 
 * <h2>Propósito</h2>
 * <p>
 * Implementa {@link ChargingStationControlPort} enviando comandos OCPP a los
 * cargadores físicos. En esta versión simulada, registra los comandos en logs.
 * En producción, enviaría comandos reales vía WebSocket OCPP.
 * 
 * <h2>Comandos OCPP Implementados</h2>
 * <ul>
 * <li><b>SetChargingProfile:</b> Limitar potencia máxima de carga</li>
 * <li><b>ClearChargingProfile:</b> Remover límites de potencia</li>
 * </ul>
 * 
 * <h2>Implementación Futura (Producción)</h2>
 * 
 * <pre>
 * // Enviar comando OCPP real
 * SetChargingProfileRequest request = new SetChargingProfileRequest();
 * request.setConnectorId(0); // 0 = todos los conectores
 * request.setChargingProfile(createPowerLimitProfile(percentageLimit));
 * 
 * ocppClient.send(chargerOcppId, request);
 * </pre>
 * 
 * @see ChargingStationControlPort
 * @since 2.0 - God Mode
 */
@Slf4j
@Component
public class OcppChargingStationControlAdapter implements ChargingStationControlPort {

    @Override
    public void broadcastPowerLimit(int percentageLimit) {
        if (percentageLimit < 0 || percentageLimit > 100) {
            throw new IllegalArgumentException("Percentage limit must be between 0 and 100");
        }

        log.info("📡 OCPP Command: SetChargingProfile(connectorId=0, powerLimit={}%)", percentageLimit);
        log.info("   → Broadcast to ALL active chargers");
        log.info("   → Profile Type: TxDefaultProfile (applies to all transactions)");
        log.info("   → Stack Level: 1 (highest priority)");

        // TODO: Implementación real con OCPP client
        // ocppClient.broadcastSetChargingProfile(createPowerLimitProfile(percentageLimit));

        log.debug("✅ OCPP command queued for transmission to {} active chargers",
                getActiveChargerCount());
    }

    @Override
    public void setStationPowerLimit(StationId stationId, double maxKw) {
        if (maxKw < 0) {
            throw new IllegalArgumentException("Max power must be positive");
        }

        log.info("📡 OCPP Command: SetChargingProfile(stationId={}, maxPower={} kW)",
                stationId.getValue(), maxKw);
        log.info("   → Target: Specific station");
        log.info("   → Limit: {} kW", maxKw);

        // TODO: Implementación real
        // String ocppId = stationRepository.findById(stationId).getOcppIdentifier();
        // ocppClient.send(ocppId, createPowerLimitProfile(maxKw));

        log.debug("✅ OCPP command sent to station {}", stationId.getValue());
    }

    @Override
    public void removeAllPowerLimits() {
        log.info("📡 OCPP Command: ClearChargingProfile(connectorId=0)");
        log.info("   → Removing ALL power limits");
        log.info("   → Chargers will return to maximum power");

        // TODO: Implementación real
        // ocppClient.broadcastClearChargingProfile();

        log.debug("✅ Power limits cleared on {} chargers", getActiveChargerCount());
    }

    /**
     * Obtiene el número de cargadores activos.
     * En producción, consultaría la base de datos o el registro de sesiones
     * activas.
     */
    private int getActiveChargerCount() {
        // TODO: Consultar repositorio de sesiones activas
        return 3; // Simulado para demo
    }
}
