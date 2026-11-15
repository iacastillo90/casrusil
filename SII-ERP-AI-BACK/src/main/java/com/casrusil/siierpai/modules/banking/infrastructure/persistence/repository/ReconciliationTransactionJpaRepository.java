package com.casrusil.siierpai.modules.banking.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.banking.infrastructure.persistence.entity.ReconciliationTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para ReconciliationTransactionEntity.
 * 
 * <p>
 * Gestiona la persistencia de los movimientos bancarios de conciliación manual
 * y facilita consultas para la reconciliación.
 * 
 * @since 2.0 - Ferrari Engine Refactoring
 */
@Repository
public interface ReconciliationTransactionJpaRepository extends JpaRepository<ReconciliationTransactionEntity, UUID> {
    
    List<ReconciliationTransactionEntity> findByCompanyId(UUID companyId);

    List<ReconciliationTransactionEntity> findByCompanyIdAndDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<ReconciliationTransactionEntity> findByCompanyIdAndReconciledFalse(UUID companyId);
    
    List<ReconciliationTransactionEntity> findByCompanyIdAndDateBetweenAndReconciledFalse(UUID companyId, LocalDate startDate, LocalDate endDate);
}