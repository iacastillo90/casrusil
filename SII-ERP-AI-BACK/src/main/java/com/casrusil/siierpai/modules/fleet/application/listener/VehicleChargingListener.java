package com.casrusil.siierpai.modules.fleet.application.listener;

import com.casrusil.siierpai.modules.energy.domain.event.ChargingSessionCompletedEvent;
import com.casrusil.siierpai.modules.energy.domain.event.ChargingSessionStartedEvent;
import com.casrusil.siierpai.modules.fleet.domain.model.Vehicle;
import com.casrusil.siierpai.modules.fleet.domain.port.out.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Listener para eventos de carga que actualiza el estado de los vehículos.
 * 
 * Responsabilidades:
 * - Actualizar estado del vehículo a CHARGING cuando inicia sesión
 * - Actualizar estado del vehículo a AVAILABLE cuando termina sesión
 * - Registrar ubicación de carga
 * - Actualizar nivel de batería (si disponible)
 */
@Component
public class VehicleChargingListener {

    private static final Logger logger = LoggerFactory.getLogger(VehicleChargingListener.class);

    private final VehicleRepository vehicleRepository;

    public VehicleChargingListener(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Maneja el inicio de una sesión de carga.
     * Actualiza el estado del vehículo a CHARGING.
     */
    @EventListener
    public void handleChargingSessionStarted(ChargingSessionStartedEvent event) {
        logger.info("Procesando inicio de carga para vehículo: sessionId={}, stationId={}",
                event.getSessionId(), event.getStationId());

        // TODO: Necesitamos mapear userId → Vehicle
        // Por ahora, esto es un placeholder que muestra la estructura
        // En producción, necesitaríamos:
        // 1. Un servicio que mapee userId a vehicleId
        // 2. O almacenar el vehicleId en el evento
        // 3. O usar el idTag del StartTransaction para buscar el vehículo

        logger.debug("Vehicle status update to CHARGING - Implementation pending vehicle lookup");

        // Ejemplo de implementación futura:
        // Optional<Vehicle> vehicleOpt =
        // vehicleRepository.findByUserId(event.getUserId());
        // if (vehicleOpt.isPresent()) {
        // Vehicle vehicle = vehicleOpt.get();
        // vehicle.startCharging(event.getStationId().getValue());
        // vehicleRepository.save(vehicle);
        // logger.info("Vehicle {} status updated to CHARGING",
        // vehicle.getLicensePlate());
        // }
    }

    /**
     * Maneja la finalización de una sesión de carga.
     * Actualiza el estado del vehículo a AVAILABLE.
     */
    @EventListener
    public void handleChargingSessionCompleted(ChargingSessionCompletedEvent event) {
        logger.info("Procesando fin de carga para vehículo: sessionId={}, totalEnergy={} kWh",
                event.getSessionId(), event.getTotalEnergyKwh());

        // TODO: Similar al método anterior, necesitamos mapear el evento al vehículo

        logger.debug("Vehicle status update to AVAILABLE - Implementation pending vehicle lookup");

        // Ejemplo de implementación futura:
        // Optional<Vehicle> vehicleOpt =
        // vehicleRepository.findByUserId(event.getUserId());
        // if (vehicleOpt.isPresent()) {
        // Vehicle vehicle = vehicleOpt.get();
        // // Calcular nivel de batería basado en energía consumida (si se conoce
        // capacidad)
        // Integer finalBatteryLevel = calculateBatteryLevel(vehicle,
        // event.getTotalEnergyKwh());
        // vehicle.completeCharging(finalBatteryLevel);
        // vehicleRepository.save(vehicle);
        // logger.info("Vehicle {} status updated to AVAILABLE, battery: {}%",
        // vehicle.getLicensePlate(), finalBatteryLevel);
        // }
    }

    /**
     * Calcula el nivel de batería basado en la energía consumida.
     * TODO: Implementar lógica real basada en capacidad de batería del vehículo.
     */
    private Integer calculateBatteryLevel(Vehicle vehicle, java.math.BigDecimal energyConsumed) {
        // Placeholder: asume que cada 10 kWh = 10% de batería
        // En realidad, esto depende de la capacidad de la batería del vehículo
        return Math.min(100, vehicle.getBatteryLevel() + energyConsumed.intValue() / 10);
    }
}
