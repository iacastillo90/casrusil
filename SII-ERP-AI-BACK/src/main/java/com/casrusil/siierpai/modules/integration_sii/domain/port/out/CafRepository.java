package com.casrusil.siierpai.modules.integration_sii.domain.port.out;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.integration_sii.domain.model.CafFolioWindow;
import com.casrusil.siierpai.modules.integration_sii.domain.model.CafInfo;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CafRepository {
    void save(CompanyId companyId, Caf caf);

    Optional<Caf> findActiveForFolio(CompanyId companyId, String tipoDte, Long folio);

    boolean existsActiveByCompanyId(CompanyId companyId);

    /**
     * Bloquea (PESSIMISTIC_WRITE) los CAF de la empresa y tipo de DTE que aún
     * tienen folios disponibles, ordenados por folio inicial ascendente.
     *
     * @param companyId empresa dueña de los CAF
     * @param tipoDte   código del tipo de DTE
     * @return ventanas de folios bloqueadas hasta el commit de la transacción
     */
    List<CafFolioWindow> lockAvailableWindows(CompanyId companyId, String tipoDte);

    /**
     * Actualiza el siguiente folio a asignar de un CAF.
     *
     * @param cafId     identificador del CAF
     * @param nextFolio siguiente folio a asignar
     */
    void advanceCurrentFolio(UUID cafId, long nextFolio);

    /**
     * Lista todos los CAF de una empresa con su estado calculado.
     *
     * @param companyId empresa dueña de los CAF
     * @return CAF con folios disponibles y estado
     */
    List<CafInfo> findAllByCompany(CompanyId companyId);
}
