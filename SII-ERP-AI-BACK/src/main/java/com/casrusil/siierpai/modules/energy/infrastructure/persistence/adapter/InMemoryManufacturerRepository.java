package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.metadata.Manufacturer;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ManufacturerId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ManufacturerRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ManufacturerRepository for testing.
 * TODO: Replace with JPA implementation for production.
 */
@Component
public class InMemoryManufacturerRepository implements ManufacturerRepository {

    private final Map<ManufacturerId, Manufacturer> storage = new ConcurrentHashMap<>();

    @Override
    public Manufacturer save(Manufacturer manufacturer) {
        storage.put(manufacturer.getId(), manufacturer);
        return manufacturer;
    }

    @Override
    public Optional<Manufacturer> findById(ManufacturerId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Manufacturer> findByName(String name) {
        return storage.values().stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Manufacturer> findAllActive() {
        return storage.values().stream()
                .filter(Manufacturer::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Manufacturer> findAll() {
        return storage.values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(ManufacturerId id) {
        storage.remove(id);
    }

    @Override
    public boolean exists(ManufacturerId id) {
        return storage.containsKey(id);
    }

    // Testing utility methods
    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
