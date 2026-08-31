package com.casrusil.siierpai.modules.fleet.domain.port.out;

import com.casrusil.siierpai.modules.fleet.domain.model.Vehicle;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de vehículos.
 */
public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(UUID id);

    Optional<Vehicle> findByRfidTag(String rfidTag);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    void delete(UUID id);
}
