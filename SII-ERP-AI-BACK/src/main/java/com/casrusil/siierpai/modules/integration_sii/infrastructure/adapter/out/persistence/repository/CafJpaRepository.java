package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository;

import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.CafEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CafJpaRepository extends JpaRepository<CafEntity, UUID> {
    Optional<CafEntity> findTopByCompanyIdOrderByInitialFolioDesc(String companyId);

    boolean existsByCompanyId(String companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CafEntity c where c.companyId = :companyId and c.tipoDte = :tipoDte "
            + "and c.currentFolio <= c.finalFolio "
            + "and (c.fechaVencimiento is null or c.fechaVencimiento >= :today) "
            + "order by c.initialFolio asc")
    List<CafEntity> findAvailableForUpdate(@Param("companyId") String companyId, @Param("tipoDte") String tipoDte,
            @Param("today") java.time.LocalDate today);

    List<CafEntity> findByCompanyIdOrderByTipoDteAscInitialFolioAsc(String companyId);

    Optional<CafEntity> findFirstByCompanyIdAndTipoDteAndInitialFolioAndFinalFolio(String companyId, String tipoDte,
            Long initialFolio, Long finalFolio);
}
