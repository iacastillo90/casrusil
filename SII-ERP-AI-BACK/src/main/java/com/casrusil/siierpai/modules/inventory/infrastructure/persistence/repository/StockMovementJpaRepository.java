package com.casrusil.siierpai.modules.inventory.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, Long> {
    List<StockMovementEntity> findByProductIdAndCompanyIdOrderByMovementDateDesc(Long productId, String companyId);
}
