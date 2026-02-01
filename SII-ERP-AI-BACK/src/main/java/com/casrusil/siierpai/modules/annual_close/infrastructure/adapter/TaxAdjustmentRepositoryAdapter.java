package com.casrusil.siierpai.modules.annual_close.infrastructure.adapter;

import com.casrusil.siierpai.modules.annual_close.domain.model.TaxAdjustment;
import com.casrusil.siierpai.modules.annual_close.domain.port.TaxAdjustmentRepositoryPort;
import com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.entity.TaxAdjustmentEntity;
import com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.repository.TaxAdjustmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaxAdjustmentRepositoryAdapter implements TaxAdjustmentRepositoryPort {

    private final TaxAdjustmentJpaRepository jpaRepository;

    @Override
    public TaxAdjustment save(TaxAdjustment adjustment) {
        TaxAdjustmentEntity entity = mapToEntity(adjustment);
        return mapToDomain(jpaRepository.save(entity));
    }

    @Override
    public List<TaxAdjustment> findByCompanyIdAndFiscalYear(String companyId, int fiscalYear) {
        return jpaRepository.findByCompanyIdAndFiscalYear(companyId, fiscalYear).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }
    
    private TaxAdjustmentEntity mapToEntity(TaxAdjustment domain) {
        TaxAdjustmentEntity entity = new TaxAdjustmentEntity();
        entity.setId(domain.getId());
        entity.setCompanyId(domain.getCompanyId());
        entity.setFiscalYear(domain.getFiscalYear());
        entity.setType(domain.getType());
        entity.setDescription(domain.getDescription());
        entity.setAmount(domain.getAmount());
        return entity;
    }
    
    private TaxAdjustment mapToDomain(TaxAdjustmentEntity entity) {
        return TaxAdjustment.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .fiscalYear(entity.getFiscalYear())
                .type(entity.getType())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .build();
    }
}
