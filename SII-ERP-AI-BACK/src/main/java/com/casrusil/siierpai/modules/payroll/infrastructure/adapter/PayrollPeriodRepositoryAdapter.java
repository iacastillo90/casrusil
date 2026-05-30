package com.casrusil.siierpai.modules.payroll.infrastructure.adapter;

import com.casrusil.siierpai.modules.payroll.domain.model.PayrollPeriod;
import com.casrusil.siierpai.modules.payroll.domain.port.PayrollPeriodRepositoryPort;
import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity.PayrollPeriodEntity;
import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.repository.PayrollPeriodJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PayrollPeriodRepositoryAdapter implements PayrollPeriodRepositoryPort {

    private final PayrollPeriodJpaRepository jpaRepository;

    @Override
    public PayrollPeriod save(PayrollPeriod period) {
        PayrollPeriodEntity entity = mapToEntity(period);
        return mapToDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PayrollPeriod> findByPeriodAndCompanyId(YearMonth period, String companyId) {
        return jpaRepository.findByPeriodAndCompanyId(period, companyId).map(this::mapToDomain);
    }

    @Override
    public Optional<PayrollPeriod> findByIdAndCompanyId(Long id, String companyId) {
        return jpaRepository.findByIdAndCompanyId(id, companyId).map(this::mapToDomain);
    }
    
    private PayrollPeriodEntity mapToEntity(PayrollPeriod domain) {
        PayrollPeriodEntity entity = new PayrollPeriodEntity();
        entity.setId(domain.getId());
        entity.setCompanyId(domain.getCompanyId());
        entity.setPeriod(domain.getPeriod());
        entity.setUfValue(domain.getUfValue());
        entity.setUtmValue(domain.getUtmValue());
        entity.setClosed(domain.isClosed());
        return entity;
    }
    
    private PayrollPeriod mapToDomain(PayrollPeriodEntity entity) {
        return PayrollPeriod.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .period(entity.getPeriod())
                .ufValue(entity.getUfValue())
                .utmValue(entity.getUtmValue())
                .isClosed(entity.isClosed())
                .build();
    }
}
