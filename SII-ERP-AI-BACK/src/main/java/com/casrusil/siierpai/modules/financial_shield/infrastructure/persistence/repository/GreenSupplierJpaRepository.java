package com.casrusil.siierpai.modules.financial_shield.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.financial_shield.infrastructure.persistence.entity.GreenSupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GreenSupplierJpaRepository extends JpaRepository<GreenSupplierEntity, UUID> {
    List<GreenSupplierEntity> findByCategoryIgnoreCase(String category);
}
