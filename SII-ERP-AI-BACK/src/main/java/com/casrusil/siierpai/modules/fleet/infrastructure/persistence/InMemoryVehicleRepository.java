package com.casrusil.siierpai.modules.fleet.infrastructure.persistence;

import com.casrusil.siierpai.modules.fleet.domain.model.Vehicle;
import com.casrusil.siierpai.modules.fleet.domain.port.out.VehicleRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del VehicleRepository.
 * TODO: Reemplazar con implementación JPA cuando se tenga la tabla de BD.
 */
@Repository
public class InMemoryVehicleRepository implements VehicleRepository {

    private final Map<UUID, Vehicle> vehicles = new ConcurrentHashMap<>();
    private final Map<String, UUID> rfidToVehicleId = new ConcurrentHashMap<>();

    @Override
    public Vehicle save(Vehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
        if (vehicle.getRfidTag() != null) {
            rfidToVehicleId.put(vehicle.getRfidTag(), vehicle.getId());
        }
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return Optional.ofNullable(vehicles.get(id));
    }

    @Override
    public Optional<Vehicle> findByRfidTag(String rfidTag) {
        UUID vehicleId = rfidToVehicleId.get(rfidTag);
        return vehicleId != null ? findById(vehicleId) : Optional.empty();
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return vehicles.values().stream()
                .filter(v -> v.getLicensePlate().equals(licensePlate))
                .findFirst();
    }

    @Override
    public void delete(UUID id) {
        Vehicle vehicle = vehicles.remove(id);
        if (vehicle != null && vehicle.getRfidTag() != null) {
            rfidToVehicleId.remove(vehicle.getRfidTag());
        }
    }
}
