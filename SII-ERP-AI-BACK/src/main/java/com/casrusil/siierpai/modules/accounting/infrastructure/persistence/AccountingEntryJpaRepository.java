package com.casrusil.siierpai.modules.accounting.infrastructure.persistence;

import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.AccountingEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;

public interface AccountingEntryJpaRepository extends JpaRepository<AccountingEntryEntity, UUID> {
    List<AccountingEntryEntity> findByCompanyId(UUID companyId);

    /**
     * 🚀 FERRARI ENGINE: Encuentra asientos en un período específico.
     * 
     * @param companyId ID de la empresa
     * @param start     Fecha inicio (inclusive)
     * @param end       Fecha fin (inclusive)
     * @return Lista de asientos en el período
     * @since 2.0 - Ferrari Engine Phase 1
     */
    List<AccountingEntryEntity> findByCompanyIdAndOccurredOnBetween(
            UUID companyId,
            Instant start,
            Instant end);

    @Query("""
                SELECT new com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement(
                    e.id,
                    e.occurredOn,
                    e.description,
                    l.debit,
                    l.credit,
                    CAST(0 AS BigDecimal)
                )
                FROM AccountingEntryEntity e
                JOIN e.lines l
                WHERE e.companyId = :companyId
                AND l.accountCode = :accountCode
                AND e.occurredOn BETWEEN :startDate AND :endDate
                ORDER BY e.occurredOn ASC
            """)
    List<AccountMovement> findMovementsByAccount(
            @Param("companyId") UUID companyId,
            @Param("accountCode") String accountCode,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    void deleteByCompanyIdAndReferenceTypeAndOccurredOnBetween(UUID companyId, String referenceType, Instant startDate,
            Instant endDate);

    List<AccountingEntryEntity> findByCompanyIdAndReferenceTypeInAndReferenceId(UUID companyId,
            List<String> referenceTypes, String referenceId);

    org.springframework.data.domain.Page<AccountingEntryEntity> findByCompanyIdOrderByOccurredOnDesc(UUID companyId,
            org.springframework.data.domain.Pageable pageable);
}
