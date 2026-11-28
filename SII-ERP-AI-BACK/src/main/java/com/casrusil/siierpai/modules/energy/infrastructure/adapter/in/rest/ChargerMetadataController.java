package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.energy.application.service.metadata.ChargerMetadataService;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModel;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModelId;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.Manufacturer;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ManufacturerId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for charger metadata management.
 */
// P0 multiempresa: apagado por defecto porque sus tablas (alertas, mantenciones, precios, campañas, catálogo) no tienen company_id.
// Sin el bean, sus rutas responden 404 como cualquier ruta inexistente. Ver ronda P6.
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "app.features.energy-admin", name = "enabled", havingValue = "true", matchIfMissing = false)
@RestController
@RequestMapping("/api/v1/metadata")
@Tag(name = "Charger Metadata", description = "Manufacturer and charger model management")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class ChargerMetadataController {

    private static final Logger logger = LoggerFactory.getLogger(ChargerMetadataController.class);

    private final ChargerMetadataService metadataService;

    public ChargerMetadataController(ChargerMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    // ==================== Manufacturer Endpoints ====================

    @PostMapping("/manufacturers")
    @Operation(summary = "Create manufacturer", description = "Register a new charger manufacturer")
    public ResponseEntity<ManufacturerDto> createManufacturer(
            @RequestBody @Valid CreateManufacturerRequest request) {

        logger.info("Creating manufacturer: {}", request.name());

        Manufacturer manufacturer = metadataService.createManufacturer(
                request.name(),
                request.website(),
                request.supportEmail(),
                request.supportPhone());

        return ResponseEntity.ok(ManufacturerDto.from(manufacturer));
    }

    @GetMapping("/manufacturers")
    @Operation(summary = "List all active manufacturers")
    public ResponseEntity<List<ManufacturerDto>> getAllManufacturers() {
        logger.info("Fetching all active manufacturers");

        List<Manufacturer> manufacturers = metadataService.getActiveManufacturers();

        return ResponseEntity.ok(manufacturers.stream()
                .map(ManufacturerDto::from)
                .toList());
    }

    @GetMapping("/manufacturers/{id}")
    @Operation(summary = "Get manufacturer by ID")
    public ResponseEntity<ManufacturerDto> getManufacturerById(@PathVariable UUID id) {
        logger.info("Fetching manufacturer: {}", id);

        try {
            Manufacturer manufacturer = metadataService.getManufacturerById(ManufacturerId.of(id));
            return ResponseEntity.ok(ManufacturerDto.from(manufacturer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/manufacturers/{id}")
    @Operation(summary = "Update manufacturer")
    public ResponseEntity<ManufacturerDto> updateManufacturer(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateManufacturerRequest request) {

        logger.info("Updating manufacturer: {}", id);

        try {
            Manufacturer updated = metadataService.updateManufacturer(
                    ManufacturerId.of(id),
                    request.name(),
                    request.website(),
                    request.supportEmail(),
                    request.supportPhone());

            return ResponseEntity.ok(ManufacturerDto.from(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/manufacturers/{id}/deactivate")
    @Operation(summary = "Deactivate manufacturer")
    public ResponseEntity<Void> deactivateManufacturer(@PathVariable UUID id) {
        logger.info("Deactivating manufacturer: {}", id);

        try {
            metadataService.deactivateManufacturer(ManufacturerId.of(id));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Charger Model Endpoints ====================

    @PostMapping("/models")
    @Operation(summary = "Create charger model", description = "Register a new charger model")
    public ResponseEntity<ChargerModelDto> createChargerModel(
            @RequestBody @Valid CreateChargerModelRequest request) {

        logger.info("Creating charger model: {}", request.modelName());

        ChargerModel model = metadataService.createChargerModel(
                ManufacturerId.of(request.manufacturerId()),
                request.modelName(),
                request.modelNumber(),
                request.numberOfConnectors(),
                request.maxPowerKw(),
                request.ocppVersion());

        return ResponseEntity.ok(ChargerModelDto.from(model));
    }

    @GetMapping("/models")
    @Operation(summary = "List all active charger models")
    public ResponseEntity<List<ChargerModelDto>> getAllModels() {
        logger.info("Fetching all active charger models");

        List<ChargerModel> models = metadataService.getActiveModels();

        return ResponseEntity.ok(models.stream()
                .map(ChargerModelDto::from)
                .toList());
    }

    @GetMapping("/models/{id}")
    @Operation(summary = "Get charger model by ID")
    public ResponseEntity<ChargerModelDto> getModelById(@PathVariable UUID id) {
        logger.info("Fetching charger model: {}", id);

        try {
            ChargerModel model = metadataService.getChargerModelById(ChargerModelId.of(id));
            return ResponseEntity.ok(ChargerModelDto.from(model));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/models/manufacturer/{manufacturerId}")
    @Operation(summary = "Get models by manufacturer")
    public ResponseEntity<List<ChargerModelDto>> getModelsByManufacturer(@PathVariable UUID manufacturerId) {
        logger.info("Fetching models for manufacturer: {}", manufacturerId);

        List<ChargerModel> models = metadataService.getModelsByManufacturer(
                ManufacturerId.of(manufacturerId));

        return ResponseEntity.ok(models.stream()
                .map(ChargerModelDto::from)
                .toList());
    }

    @GetMapping("/models/ocpp/{version}")
    @Operation(summary = "Get models by OCPP version")
    public ResponseEntity<List<ChargerModelDto>> getModelsByOcppVersion(@PathVariable String version) {
        logger.info("Fetching models for OCPP version: {}", version);

        List<ChargerModel> models = metadataService.getModelsByOcppVersion(version);

        return ResponseEntity.ok(models.stream()
                .map(ChargerModelDto::from)
                .toList());
    }

    @PutMapping("/models/{id}")
    @Operation(summary = "Update charger model")
    public ResponseEntity<ChargerModelDto> updateChargerModel(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateChargerModelRequest request) {

        logger.info("Updating charger model: {}", id);

        try {
            ChargerModel updated = metadataService.updateChargerModel(
                    ChargerModelId.of(id),
                    request.modelName(),
                    request.modelNumber(),
                    request.numberOfConnectors(),
                    request.maxPowerKw());

            return ResponseEntity.ok(ChargerModelDto.from(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/models/{id}/firmware")
    @Operation(summary = "Update firmware version")
    public ResponseEntity<ChargerModelDto> updateFirmware(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateFirmwareRequest request) {

        logger.info("Updating firmware for model {}: {}", id, request.firmwareVersion());

        try {
            ChargerModel updated = metadataService.updateFirmware(
                    ChargerModelId.of(id),
                    request.firmwareVersion());

            return ResponseEntity.ok(ChargerModelDto.from(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Note: Activate/deactivate not implemented in service yet
    // TODO: Add activate/deactivate methods to ChargerMetadataService

    // ==================== DTOs ====================

    public record CreateManufacturerRequest(
            @NotBlank String name,
            String website,
            String supportEmail,
            String supportPhone) {
    }

    public record UpdateManufacturerRequest(
            String name,
            String website,
            String supportEmail,
            String supportPhone) {
    }

    public record ManufacturerDto(
            UUID id,
            String name,
            String website,
            String supportEmail,
            String supportPhone,
            boolean active) {
        public static ManufacturerDto from(Manufacturer manufacturer) {
            return new ManufacturerDto(
                    manufacturer.getId().getValue(),
                    manufacturer.getName(),
                    manufacturer.getWebsite(),
                    manufacturer.getSupportEmail(),
                    manufacturer.getSupportPhone(),
                    manufacturer.isActive());
        }
    }

    public record CreateChargerModelRequest(
            @NotNull UUID manufacturerId,
            @NotBlank String modelName,
            @NotBlank String modelNumber,
            @Positive int numberOfConnectors,
            @NotNull @Positive BigDecimal maxPowerKw,
            @NotBlank String ocppVersion) {
    }

    public record UpdateChargerModelRequest(
            String modelName,
            String modelNumber,
            @Positive int numberOfConnectors,
            @Positive BigDecimal maxPowerKw) {
    }

    public record UpdateFirmwareRequest(
            @NotBlank String firmwareVersion) {
    }

    public record ChargerModelDto(
            UUID id,
            UUID manufacturerId,
            String modelName,
            String modelNumber,
            int numberOfConnectors,
            BigDecimal maxPowerKw,
            String firmwareVersion,
            String ocppVersion,
            boolean supportsSmartCharging,
            boolean supportsReservation,
            boolean active) {
        public static ChargerModelDto from(ChargerModel model) {
            return new ChargerModelDto(
                    model.getId().getValue(),
                    model.getManufacturerId().getValue(),
                    model.getModelName(),
                    model.getModelNumber(),
                    model.getNumberOfConnectors(),
                    model.getMaxPowerKw(),
                    model.getFirmwareVersion(),
                    model.getOcppVersion(),
                    model.supportsSmartCharging(),
                    model.supportsReservation(),
                    model.isActive());
        }
    }
}
