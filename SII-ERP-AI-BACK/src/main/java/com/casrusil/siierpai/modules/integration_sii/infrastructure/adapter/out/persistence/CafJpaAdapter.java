package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.CafEntity;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository.CafJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CafJpaAdapter implements CafRepository {

    private final CafJpaRepository jpaRepo;
    // CafParser is needed to deserialize the raw XML back into a Caf domain object
    private final com.casrusil.siierpai.modules.integration_sii.infrastructure.parser.CafParser cafParser;

    public CafJpaAdapter(CafJpaRepository jpaRepo,
                         com.casrusil.siierpai.modules.integration_sii.infrastructure.parser.CafParser cafParser) {
        this.jpaRepo = jpaRepo;
        this.cafParser = cafParser;
    }

    @Override
    public void save(CompanyId companyId, Caf caf) {
        CafEntity e = new CafEntity();
        e.setId(UUID.randomUUID());
        e.setCompanyId(companyId.value().toString());
        e.setXmlContent(caf.xmlContent());
        e.setInitialFolio(caf.rangoDesde());
        e.setFinalFolio(caf.rangoHasta());
        e.setCurrentFolio(caf.rangoDesde());
        jpaRepo.save(e);
    }

    @Override
    public Optional<Caf> findActiveForFolio(CompanyId companyId, String tipoDte, Long folio) {
        return jpaRepo.findAll().stream()
            .filter(e -> e.getCompanyId().equals(companyId.value().toString()))
            .filter(e -> folio >= e.getInitialFolio() && folio <= e.getFinalFolio())
            .findFirst()
            .map(e -> {
                try {
                    return cafParser.parse(e.getXmlContent());
                } catch (Exception ex) {
                    throw new RuntimeException("Error deserializing CAF from DB", ex);
                }
            });
    }
}