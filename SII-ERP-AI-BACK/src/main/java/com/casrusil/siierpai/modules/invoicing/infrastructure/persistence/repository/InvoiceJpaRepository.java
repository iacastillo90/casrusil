package com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para facturas.
 * 
 * <p>
 * Maneja el almacenamiento y recuperación de documentos tributarios.
 * Incluye consultas para búsqueda por folio, RUT emisor/receptor y fecha.
 * 
 * @since 1.0
 */
@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {
        List<InvoiceEntity> findAllByCompanyId(UUID companyId);

        boolean existsByCompanyIdAndTypeCodeAndFolioAndIssuerRut(UUID companyId, Integer typeCode, Long folio,
                        String issuerRut);

        boolean existsByCompanyIdAndOrigin(UUID companyId, String origin);

        java.util.Optional<InvoiceEntity> findFirstByCompanyIdAndTypeCodeAndFolio(UUID companyId, Integer typeCode,
                        Long folio);

        java.util.Optional<InvoiceEntity> findByCompanyIdAndTypeCodeAndFolio(UUID companyId, Integer typeCode,
                        Long folio);

        java.util.Optional<InvoiceEntity> findByCompanyIdAndTypeCodeAndFolioAndIssuerRut(UUID companyId,
                        Integer typeCode,
                        Long folio, String issuerRut);

        void deleteByCompanyIdAndDateBetween(UUID companyId, java.time.LocalDate start, java.time.LocalDate end);

        @Query("select max(i.folio) from InvoiceEntity i where i.companyId = :companyId and i.typeCode = :typeCode and i.folio between :fromFolio and :toFolio")
        Long findMaxFolioInRange(@Param("companyId") UUID companyId, @Param("typeCode") Integer typeCode,
                        @Param("fromFolio") Long fromFolio, @Param("toFolio") Long toFolio);

        org.springframework.data.domain.Page<InvoiceEntity> findByCompanyIdOrderByDateDesc(UUID companyId,
                        org.springframework.data.domain.Pageable pageable);
}
