package com.casrusil.siierpai.modules.integration_sii.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.CafInfo;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Consulta el estado de los CAF de la empresa actual.
 *
 * @since 1.0
 */
@Service
public class CafQueryService {

    private final CafRepository cafRepository;

    public CafQueryService(CafRepository cafRepository) {
        this.cafRepository = cafRepository;
    }

    /**
     * Lista los CAF de la empresa actual ordenados por tipo de DTE y folio
     * inicial.
     *
     * @return CAF con su estado calculado
     */
    public List<CafInfo> listCafs() {
        return cafRepository.findAllByCompany(CompanyContext.requireCompanyId()).stream()
                .sorted(Comparator
                        .comparing(CafInfo::tipoDte, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CafInfo::folioDesde, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
