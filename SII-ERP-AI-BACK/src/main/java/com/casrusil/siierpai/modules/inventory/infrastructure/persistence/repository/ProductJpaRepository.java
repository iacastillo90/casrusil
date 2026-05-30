package com.casrusil.siierpai.modules.inventory.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductEntity> findByIdAndCompanyId(Long id, String companyId);
    Optional<ProductEntity> findBySkuAndCompanyId(String sku, String companyId);
    List<ProductEntity> findAllByCompanyId(String companyId);
    void deleteByIdAndCompanyId(Long id, String companyId);
}
