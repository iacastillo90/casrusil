package com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper;

import com.casrusil.siierpai.modules.energy.domain.model.*;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper: Conversión bidireccional entre ChargingStation (dominio) y
 * ChargingStationEntity (JPA).
 * 
 * Patrón: Mapper Pattern
 * Responsabilidad: Mantener el dominio puro sin anotaciones JPA.
 */
@Component
public class ChargingStationMapper {

    // ========== DOMAIN → JPA ==========

    public ChargingStationEntity toEntity(ChargingStation domain) {
        if (domain == null) {
            return null;
        }

        ChargingStationEntity entity = new ChargingStationEntity(
                domain.getStationId().getValue(),
                domain.getName(),
                toLocationEmbeddable(domain.getLocation()),
                domain.getCompanyId(),
                domain.getStatus(),
                domain.isActive(),
                domain.getCreatedAt());

        entity.setLastMaintenanceDate(domain.getLastMaintenanceDate());

        // Mapear chargers
        domain.getChargers().forEach(charger -> {
            ChargerEntity chargerEntity = toChargerEntity(charger);
            entity.addCharger(chargerEntity);
        });

        return entity;
    }

    public ChargerEntity toChargerEntity(Charger domain) {
        if (domain == null) {
            return null;
        }

        ChargerEntity entity = new ChargerEntity(
                domain.getOcppId().getValue(),
                domain.getName(),
                domain.getAlias(),
                domain.getManufacturer(),
                domain.getModel(),
                domain.getStatus(),
                domain.isActive(),
                domain.getInstallationDate());

        entity.setLastHeartbeat(domain.getLastHeartbeat());

        // Mapear connectors
        domain.getConnectors().forEach(connector -> {
            ConnectorEntity connectorEntity = toConnectorEntity(connector);
            entity.addConnector(connectorEntity);
        });

        return entity;
    }

    public ConnectorEntity toConnectorEntity(Connector domain) {
        if (domain == null) {
            return null;
        }

        return new ConnectorEntity(
                domain.getConnectorId().getValue(),
                domain.getAlias(),
                domain.getType(),
                domain.getStatus(),
                toElectricalSpecEmbeddable(domain.getElectricalSpec()),
                domain.isActive(),
                domain.getLastStatusChange());
    }

    public LocationEmbeddable toLocationEmbeddable(StationLocation domain) {
        if (domain == null) {
            return null;
        }

        return new LocationEmbeddable(
                domain.getLatitude(),
                domain.getLongitude(),
                domain.getAddress(),
                domain.getCity(),
                domain.getCountry());
    }

    public ElectricalSpecEmbeddable toElectricalSpecEmbeddable(ElectricalSpecification domain) {
        if (domain == null) {
            return null;
        }

        return new ElectricalSpecEmbeddable(
                domain.getMaxVoltage(),
                domain.getMaxPower(),
                domain.getMaxCurrent());
    }

    // ========== JPA → DOMAIN ==========

    public ChargingStation toDomain(ChargingStationEntity entity) {
        if (entity == null) {
            return null;
        }

        var chargers = entity.getChargers().stream()
                .map(this::toChargerDomain)
                .collect(Collectors.toList());

        return ChargingStation.builder()
                .stationId(StationId.of(entity.getId()))
                .name(entity.getName())
                .location(toLocationDomain(entity.getLocation()))
                .companyId(entity.getCompanyId())
                .status(entity.getStatus())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .lastMaintenanceDate(entity.getLastMaintenanceDate())
                .chargers(chargers)
                .build();
    }

    public Charger toChargerDomain(ChargerEntity entity) {
        if (entity == null) {
            return null;
        }

        var connectors = entity.getConnectors().stream()
                .map(this::toConnectorDomain)
                .collect(Collectors.toList());

        Charger charger = Charger.builder()
                .ocppId(OcppIdentifier.of(entity.getOcppId()))
                .name(entity.getName())
                .alias(entity.getAlias())
                .manufacturer(entity.getManufacturer())
                .model(entity.getModel())
                .status(entity.getStatus())
                .active(entity.getActive())
                .installationDate(entity.getInstallationDate())
                .connectors(connectors)
                .build();

        // Restaurar lastHeartbeat manualmente (no está en el builder)
        // En una implementación real, podrías usar reflection o agregar al builder

        return charger;
    }

    public Connector toConnectorDomain(ConnectorEntity entity) {
        if (entity == null) {
            return null;
        }

        return Connector.builder()
                .connectorId(ConnectorId.of(entity.getConnectorNumber()))
                .alias(entity.getAlias())
                .type(entity.getType())
                .electricalSpec(toElectricalSpecDomain(entity.getElectricalSpec()))
                .status(entity.getStatus())
                .active(entity.getActive())
                .build();
    }

    public StationLocation toLocationDomain(LocationEmbeddable entity) {
        if (entity == null) {
            return null;
        }

        return StationLocation.of(
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getAddress(),
                entity.getCity(),
                entity.getCountry());
    }

    public ElectricalSpecification toElectricalSpecDomain(ElectricalSpecEmbeddable entity) {
        if (entity == null) {
            return null;
        }

        return ElectricalSpecification.of(
                entity.getMaxVoltage(),
                entity.getMaxPower(),
                entity.getMaxCurrent());
    }
}
