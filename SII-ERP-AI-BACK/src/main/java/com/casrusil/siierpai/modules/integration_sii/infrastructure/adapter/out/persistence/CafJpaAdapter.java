package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.integration_sii.domain.model.CafFolioWindow;
import com.casrusil.siierpai.modules.integration_sii.domain.model.CafInfo;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.CafEntity;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository.CafJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CafJpaAdapter implements CafRepository {

    private final CafJpaRepository jpaRepo;
    // CafParser is needed to deserialize the raw XML back into a Caf domain object
    private final com.casrusil.siierpai.modules.integration_sii.infrastructure.parser.CafParser cafParser;
    private final long umbralAgotamiento;

    public CafJpaAdapter(CafJpaRepository jpaRepo,
                         com.casrusil.siierpai.modules.integration_sii.infrastructure.parser.CafParser cafParser,
                         @Value("${sii.caf.umbral-agotamiento:10}") long umbralAgotamiento) {
        this.jpaRepo = jpaRepo;
        this.cafParser = cafParser;
        this.umbralAgotamiento = umbralAgotamiento;
    }

    @Override
    public void save(CompanyId companyId, Caf caf) {
        String company = companyId.value().toString();
        Optional<CafEntity> existing = jpaRepo.findFirstByCompanyIdAndTipoDteAndInitialFolioAndFinalFolio(
                company, caf.tipoDte(), caf.rangoDesde(), caf.rangoHasta());
        if (existing.isPresent()) {
            // Recarga idempotente: refresca XML y fechas sin reiniciar el puntero
            // de folios ya consumidos.
            CafEntity e = existing.get();
            e.setXmlContent(caf.xmlContent());
            e.setFechaAutorizacion(caf.fechaAutorizacion());
            e.setFechaVencimiento(caf.fechaVencimiento());
            jpaRepo.save(e);
            return;
        }

        CafEntity e = new CafEntity();
        e.setId(UUID.randomUUID());
        e.setCompanyId(company);
        e.setXmlContent(caf.xmlContent());
        e.setInitialFolio(caf.rangoDesde());
        e.setFinalFolio(caf.rangoHasta());
        e.setCurrentFolio(caf.rangoDesde());
        e.setTipoDte(caf.tipoDte());
        e.setFechaAutorizacion(caf.fechaAutorizacion());
        e.setFechaVencimiento(caf.fechaVencimiento());
        jpaRepo.save(e);
    }

    @Override
    public boolean existsActiveByCompanyId(CompanyId companyId) {
        return jpaRepo.existsByCompanyId(companyId.value().toString());
    }

    @Override
    public Optional<Caf> findActiveForFolio(CompanyId companyId, String tipoDte, Long folio) {
        String company = companyId.value().toString();
        return jpaRepo.findAll().stream()
            .filter(e -> e.getCompanyId().equals(company))
            .filter(e -> tipoDte != null && tipoDte.equals(e.getTipoDte()))
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

    @Override
    public List<CafFolioWindow> lockAvailableWindows(CompanyId companyId, String tipoDte) {
        return jpaRepo.findAvailableForUpdate(companyId.value().toString(), tipoDte, LocalDate.now()).stream()
                .map(e -> new CafFolioWindow(e.getId(), e.getInitialFolio(), e.getFinalFolio(), e.getCurrentFolio()))
                .toList();
    }

    @Override
    public void advanceCurrentFolio(UUID cafId, long nextFolio) {
        jpaRepo.findById(cafId).ifPresent(e -> {
            e.setCurrentFolio(nextFolio);
            jpaRepo.save(e);
        });
    }

    @Override
    public List<CafInfo> findAllByCompany(CompanyId companyId) {
        LocalDate today = LocalDate.now();
        return jpaRepo.findByCompanyIdOrderByTipoDteAscInitialFolioAsc(companyId.value().toString()).stream()
                .map(e -> {
                    long disponibles = Math.max(0L, e.getFinalFolio() - e.getCurrentFolio() + 1);
                    return new CafInfo(
                            e.getId(),
                            e.getTipoDte(),
                            e.getInitialFolio(),
                            e.getFinalFolio(),
                            e.getCurrentFolio(),
                            disponibles,
                            e.getFechaAutorizacion(),
                            e.getFechaVencimiento(),
                            CafInfo.evaluateEstado(disponibles, e.getFechaVencimiento(), today, umbralAgotamiento));
                })
                .toList();
    }
}
