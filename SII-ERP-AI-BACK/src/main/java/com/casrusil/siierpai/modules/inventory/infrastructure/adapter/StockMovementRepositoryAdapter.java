package com.casrusil.siierpai.modules.inventory.infrastructure.adapter;

import com.casrusil.siierpai.modules.inventory.domain.model.StockMovement;
import com.casrusil.siierpai.modules.inventory.domain.port.StockMovementRepositoryPort;
import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.entity.StockMovementEntity;
import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.repository.StockMovementJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StockMovementRepositoryAdapter implements StockMovementRepositoryPort {

    private final StockMovementJpaRepository jpaRepository;

    @Override
    public StockMovement save(StockMovement movement) {
        StockMovementEntity entity = mapToEntity(movement);
        StockMovementEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public List<StockMovement> findByProductIdAndCompanyId(Long productId, String companyId) {
        return jpaRepository.findByProductIdAndCompanyIdOrderByMovementDateDesc(productId, companyId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private StockMovementEntity mapToEntity(StockMovement domain) {
        StockMovementEntity entity = new StockMovementEntity();
        entity.setId(domain.getId());
        entity.setCompanyId(domain.getCompanyId());
        entity.setProductId(domain.getProductId());
        entity.setType(domain.getType());
        entity.setQuantity(domain.getQuantity());
        entity.setUnitCost(domain.getUnitCost());
        entity.setTotalCost(domain.getTotalCost());
        entity.setMovementDate(domain.getMovementDate());
        entity.setReferenceDocument(domain.getReferenceDocument());
        entity.setDescription(domain.getDescription());
        return entity;
    }

    private StockMovement mapToDomain(StockMovementEntity entity) {
        return StockMovement.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .productId(entity.getProductId())
                .type(entity.getType())
                .quantity(entity.getQuantity())
                .unitCost(entity.getUnitCost())
                .totalCost(entity.getTotalCost())
                .movementDate(entity.getMovementDate())
                .referenceDocument(entity.getReferenceDocument())
                .description(entity.getDescription())
                .build();
    }
}
