package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModel;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModelId;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ManufacturerId;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for ChargerModel persistence.
 */
public interface ChargerModelRepository {

    /**
     * Saves a charger model.
     */
    ChargerModel save(ChargerModel model);

    /**
     * Finds a charger model by its ID.
     */
    Optional<ChargerModel> findById(ChargerModelId id);

    /**
     * Finds all charger models by manufacturer.
     */
    List<ChargerModel> findByManufacturerId(ManufacturerId manufacturerId);

    /**
     * Finds all active charger models.
     */
    List<ChargerModel> findAllActive();

    /**
     * Finds all charger models.
     */
    List<ChargerModel> findAll();

    /**
     * Searches charger models by name.
     */
    List<ChargerModel> searchByModelName(String searchTerm);

    /**
     * Finds all models by OCPP version.
     */
    List<ChargerModel> findByOcppVersion(String ocppVersion);

    void delete(ChargerModelId id);

    boolean exists(ChargerModelId id);
}
