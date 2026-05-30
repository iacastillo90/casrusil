package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModel;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ChargerModelId;
import com.casrusil.siierpai.modules.energy.domain.model.metadata.ManufacturerId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargerModelRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ChargerModelRepository for testing.
 * TODO: Replace with JPA implementation for production.
 */
@Component
public class InMemoryChargerModelRepository implements ChargerModelRepository {

    private final Map<ChargerModelId, ChargerModel> storage = new ConcurrentHashMap<>();

    @Override
    public ChargerModel save(ChargerModel model) {
        storage.put(model.getId(), model);
        return model;
    }

    @Override
    public Optional<ChargerModel> findById(ChargerModelId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ChargerModel> findByManufacturerId(ManufacturerId manufacturerId) {
        return storage.values().stream()
                .filter(model -> model.getManufacturerId().equals(manufacturerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargerModel> findAllActive() {
        return storage.values().stream()
                .filter(ChargerModel::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargerModel> findAll() {
        return storage.values().stream().collect(Collectors.toList());
    }

    @Override
    public List<ChargerModel> searchByModelName(String searchTerm) {
        String lowerSearch = searchTerm.toLowerCase();
        return storage.values().stream()
                .filter(model -> model.getModelName().toLowerCase().contains(lowerSearch) ||
                        model.getModelNumber().toLowerCase().contains(lowerSearch))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargerModel> findByOcppVersion(String ocppVersion) {
        return storage.values().stream()
                .filter(model -> model.getOcppVersion().equals(ocppVersion))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(ChargerModelId id) {
        storage.remove(id);
    }

    @Override
    public boolean exists(ChargerModelId id) {
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
