package com.casrusil.siierpai.modules.fleet.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles", schema = "fleet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "rfid_tag", unique = true)
    private String rfidTag;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "current_charging_station_id")
    private UUID currentChargingStationId;

    @Column(name = "last_charging_session")
    private LocalDateTime lastChargingSession;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
