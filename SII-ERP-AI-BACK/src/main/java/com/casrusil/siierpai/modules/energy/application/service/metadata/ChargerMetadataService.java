package com.casrusil.siierpai.modules.energy.application.service.metadata;

import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModel;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModelId;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.Manufacturer;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ManufacturerId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargerModelRepository;
import com.casrusil.siierpai.modules.energy.domain.port.out.ManufacturerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing charger metadata (manufacturers and models).
 */
@Service
@Transactional
public class ChargerMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(ChargerMetadataService.class);

    private final ManufacturerRepository manufacturerRepository;
    private final ChargerModelRepository chargerModelRepository;

    public ChargerMetadataService(
            ManufacturerRepository manufacturerRepository,
            ChargerModelRepository chargerModelRepository) {
        this.manufacturerRepository = manufacturerRepository;
        this.chargerModelRepository = chargerModelRepository;
    }

    // ========== MANUFACTURER MANAGEMENT ==========

    /**
     * Creates a new manufacturer.
     */
    public Manufacturer createManufacturer(
            String name,
            String website,
            String supportEmail,
            String supportPhone) {

        logger.info("Creating manufacturer: {}", name);

        // Check if manufacturer already exists
        manufacturerRepository.findByName(name).ifPresent(existing -> {
            throw new IllegalArgumentException("Manufacturer with name '" + name + "' already exists");
        });

        Manufacturer manufacturer = Manufacturer.create(name, website, supportEmail, supportPhone);
        return manufacturerRepository.save(manufacturer);
    }

    /**
     * Updates a manufacturer.
     */
    public Manufacturer updateManufacturer(
            ManufacturerId id,
            String name,
            String website,
            String supportEmail,
            String supportPhone) {

        logger.info("Updating manufacturer: {}", id);

        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found: " + id));

        manufacturer.update(name, website, supportEmail, supportPhone);
        return manufacturerRepository.save(manufacturer);
    }

    /**
     * Deactivates a manufacturer.
     */
    public void deactivateManufacturer(ManufacturerId id) {
        logger.info("Deactivating manufacturer: {}", id);

        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found: " + id));

        manufacturer.deactivate();
        manufacturerRepository.save(manufacturer);
    }

    /**
     * Gets all active manufacturers.
     */
    @Transactional(readOnly = true)
    public List<Manufacturer> getActiveManufacturers() {
        return manufacturerRepository.findAllActive();
    }

    // ========== CHARGER MODEL MANAGEMENT ==========

    /**
     * Creates a new charger model.
     */
    public ChargerModel createChargerModel(
            ManufacturerId manufacturerId,
            String modelName,
            String modelNumber,
            int numberOfConnectors,
            BigDecimal maxPowerKw,
            String ocppVersion) {

        logger.info("Creating charger model: {} for manufacturer: {}", modelName, manufacturerId);

        // Verify manufacturer exists
        manufacturerRepository.findById(manufacturerId)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found: " + manufacturerId));

        ChargerModel model = ChargerModel.create(
                manufacturerId,
                modelName,
                modelNumber,
                numberOfConnectors,
                maxPowerKw,
                ocppVersion);

        return chargerModelRepository.save(model);
    }

    /**
     * Updates charger model specifications.
     */
    public ChargerModel updateChargerModel(
            ChargerModelId id,
            String modelName,
            String modelNumber,
            int numberOfConnectors,
            BigDecimal maxPowerKw) {

        logger.info("Updating charger model: {}", id);

        ChargerModel model = chargerModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Charger model not found: " + id));

        model.updateSpecifications(modelName, modelNumber, numberOfConnectors, maxPowerKw);
        return chargerModelRepository.save(model);
    }

    /**
     * Updates firmware version for a model.
     */
    public ChargerModel updateFirmware(ChargerModelId id, String firmwareVersion) {
        logger.info("Updating firmware for model {}: {}", id, firmwareVersion);

        ChargerModel model = chargerModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Charger model not found: " + id));

        model.updateFirmware(firmwareVersion);
        return chargerModelRepository.save(model);
    }

    /**
     * Gets all models by manufacturer.
     */
    @Transactional(readOnly = true)
    public List<ChargerModel> getModelsByManufacturer(ManufacturerId manufacturerId) {
        return chargerModelRepository.findByManufacturerId(manufacturerId);
    }

    /**
     * Gets all active charger models.
     */
    @Transactional(readOnly = true)
    public List<ChargerModel> getActiveModels() {
        return chargerModelRepository.findAllActive();
    }

    /**
     * Gets models by OCPP version.
     */
    @Transactional(readOnly = true)
    public List<ChargerModel> getModelsByOcppVersion(String ocppVersion) {
        return chargerModelRepository.findByOcppVersion(ocppVersion);
    }

    /**
     * Gets manufacturer by ID.
     */
    @Transactional(readOnly = true)
    public Manufacturer getManufacturerById(ManufacturerId id) {
        return manufacturerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found: " + id));
    }

    /**
     * Gets charger model by ID.
     */
    @Transactional(readOnly = true)
    public ChargerModel getChargerModelById(ChargerModelId id) {
        return chargerModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Charger model not found: " + id));
    }
}
