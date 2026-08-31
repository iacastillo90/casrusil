package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.metadata.Manufacturer;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ManufacturerId;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for Manufacturer persistence.
 */
public interface ManufacturerRepository {

    /**
     * Saves a manufacturer.
     */
    Manufacturer save(Manufacturer manufacturer);

    /**
     * Finds a manufacturer by its ID.
     */
    Optional<Manufacturer> findById(ManufacturerId id);

    /**
     * Finds a manufacturer by name.
     */
    Optional<Manufacturer> findByName(String name);

    /**
     * Finds all manufacturers.
     */
    List<Manufacturer> findAll();

    /**
     * Finds all active manufacturers.
     */
    List<Manufacturer> findAllActive();

    /**
     * Deletes a manufacturer.
     */
    void delete(ManufacturerId id);

    /**
     * Checks if a manufacturer exists.
     */
    boolean exists(ManufacturerId id);
}
