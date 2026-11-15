package com.casrusil.siierpai.modules.payroll.infrastructure.adapter;

import com.casrusil.siierpai.modules.payroll.domain.model.Payslip;
import com.casrusil.siierpai.modules.payroll.domain.port.PayslipRepositoryPort;
import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity.PayslipEntity;
import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.repository.PayslipJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PayslipRepositoryAdapter implements PayslipRepositoryPort {

    private final PayslipJpaRepository jpaRepository;

    @Override
    public Payslip save(Payslip payslip) {
        PayslipEntity entity = mapToEntity(payslip);
        return mapToDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Payslip> findByPeriodIdAndCompanyId(Long periodId, String companyId) {
        return jpaRepository.findByPeriodIdAndCompanyId(periodId, companyId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payslip> findByEmployeeId(String employeeId) {
        try {
            UUID uuid = UUID.fromString(employeeId);
            return jpaRepository.findByEmployeeId(uuid).stream()
                    .map(this::mapToDomain)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }
    
    private PayslipEntity mapToEntity(Payslip domain) {
        PayslipEntity entity = new PayslipEntity();
        entity.setId(domain.getId());
        entity.setCompanyId(domain.getCompanyId());
        entity.setEmployeeId(domain.getEmployeeId());
        entity.setPeriodId(domain.getPeriodId());
        entity.setBaseSalary(domain.getBaseSalary());
        entity.setGratification(domain.getGratification());
        entity.setTaxableIncome(domain.getTaxableIncome());
        entity.setNonTaxableIncome(domain.getNonTaxableIncome());
        entity.setAfpDiscount(domain.getAfpDiscount());
        entity.setHealthDiscount(domain.getHealthDiscount());
        entity.setUnemploymentInsurance(domain.getUnemploymentInsurance());
        entity.setSingleTax(domain.getSingleTax());
        entity.setTotalDiscounts(domain.getTotalDiscounts());
        entity.setNetSalary(domain.getNetSalary());
        return entity;
    }
    
    private Payslip mapToDomain(PayslipEntity entity) {
        return Payslip.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .employeeId(entity.getEmployeeId())
                .periodId(entity.getPeriodId())
                .baseSalary(entity.getBaseSalary())
                .gratification(entity.getGratification())
                .taxableIncome(entity.getTaxableIncome())
                .nonTaxableIncome(entity.getNonTaxableIncome())
                .afpDiscount(entity.getAfpDiscount())
                .healthDiscount(entity.getHealthDiscount())
                .unemploymentInsurance(entity.getUnemploymentInsurance())
                .singleTax(entity.getSingleTax())
                .totalDiscounts(entity.getTotalDiscounts())
                .netSalary(entity.getNetSalary())
                .build();
    }
}
