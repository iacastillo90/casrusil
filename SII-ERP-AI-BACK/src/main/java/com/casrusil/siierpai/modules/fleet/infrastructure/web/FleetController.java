package com.casrusil.siierpai.modules.fleet.infrastructure.web;

import com.casrusil.siierpai.modules.fleet.domain.model.Vehicle;
import com.casrusil.siierpai.modules.fleet.domain.model.VehicleStatus;
import com.casrusil.siierpai.modules.fleet.domain.port.out.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Métricas agregadas de la flota.
 *
 * <p>NOTA: la entidad {@code VehicleEntity} todavía no tiene {@code companyId},
 * por lo que estas métricas son globales y no por empresa.</p>
 */
@RestController
@RequestMapping("/api/v1/fleet")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class FleetController {

    private final VehicleRepository vehicleRepository;

    @GetMapping("/metrics")
    public ResponseEntity<FleetMetrics> getMetrics() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        long total = vehicles.size();
        long enRoute = count(vehicles, VehicleStatus.IN_USE);
        long charging = count(vehicles, VehicleStatus.CHARGING);
        long available = count(vehicles, VehicleStatus.AVAILABLE);
        long maintenance = count(vehicles, VehicleStatus.OUT_OF_SERVICE);

        long active = enRoute + charging + available;
        double efficiency = total == 0 ? 0.0 : Math.round((active * 10000.0) / total) / 100.0;

        return ResponseEntity.ok(new FleetMetrics(active, total, enRoute, efficiency, maintenance));
    }

    private long count(List<Vehicle> vehicles, VehicleStatus status) {
        return vehicles.stream().filter(v -> v.getStatus() == status).count();
    }

    public record FleetMetrics(
            long activeVehicles,
            long totalVehicles,
            long enRoute,
            double efficiencyPercent,
            long maintenanceScheduled) {
    }
}
