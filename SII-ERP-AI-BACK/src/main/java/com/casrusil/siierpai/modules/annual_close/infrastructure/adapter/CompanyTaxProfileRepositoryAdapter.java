package com.casrusil.siierpai.modules.annual_close.infrastructure.adapter;

import com.casrusil.siierpai.modules.annual_close.domain.model.CompanyTaxProfile;
import com.casrusil.siierpai.modules.annual_close.domain.port.CompanyTaxProfileRepositoryPort;
import com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.entity.CompanyTaxProfileEntity;
import com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.repository.CompanyTaxProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompanyTaxProfileRepositoryAdapter implements CompanyTaxProfileRepositoryPort {

    private final CompanyTaxProfileJpaRepository jpaRepository;

    @Override
    public CompanyTaxProfile save(CompanyTaxProfile profile) {
        CompanyTaxProfileEntity entity = mapToEntity(profile);
        return mapToDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<CompanyTaxProfile> findByCompanyId(String companyId) {
        return jpaRepository.findById(companyId).map(this::mapToDomain);
    }
    
    private CompanyTaxProfileEntity mapToEntity(CompanyTaxProfile domain) {
        CompanyTaxProfileEntity entity = new CompanyTaxProfileEntity();
        entity.setCompanyId(domain.getCompanyId());
        entity.setRegime(domain.getRegime());
        entity.setPpmRate(domain.getPpmRate());
        return entity;
    }
    
    private CompanyTaxProfile mapToDomain(CompanyTaxProfileEntity entity) {
        return CompanyTaxProfile.builder()
                .companyId(entity.getCompanyId())
                .regime(entity.getRegime())
                .ppmRate(entity.getPpmRate())
                .build();
    }
}
