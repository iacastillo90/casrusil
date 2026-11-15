package com.casrusil.siierpai.modules.financial_shield.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.financial_shield.domain.port.out.GreenSupplierRepository;
import com.casrusil.siierpai.modules.financial_shield.infrastructure.persistence.entity.GreenSupplierEntity;
import com.casrusil.siierpai.modules.financial_shield.infrastructure.persistence.repository.GreenSupplierJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GreenSupplierRepositoryAdapter implements GreenSupplierRepository {

    private final GreenSupplierJpaRepository repository;

    public GreenSupplierRepositoryAdapter(GreenSupplierJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> findGreenAlternativeByCategory(String category) {
        List<GreenSupplierEntity> suppliers = repository.findByCategoryIgnoreCase(category);
        if (suppliers.isEmpty()) {
            return Optional.empty();
        }
        // Return the first green supplier found for the category
        return Optional.of(suppliers.get(0).getName());
    }
}
