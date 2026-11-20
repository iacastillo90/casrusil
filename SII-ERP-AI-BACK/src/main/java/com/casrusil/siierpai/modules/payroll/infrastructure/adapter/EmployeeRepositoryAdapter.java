package com.casrusil.siierpai.modules.payroll.infrastructure.adapter;

import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import com.casrusil.siierpai.modules.payroll.domain.port.EmployeeRepositoryPort;
import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity.EmployeeEntity;
import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.repository.EmployeeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmployeeRepositoryAdapter implements EmployeeRepositoryPort {

    private final EmployeeJpaRepository jpaRepository;

    @Override
    public Employee save(Employee employee) {
        EmployeeEntity entity = mapToEntity(employee);
        return mapToDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Employee> findByIdAndCompanyId(Long id, String companyId) {
        return jpaRepository.findByIdAndCompanyId(id, companyId).map(this::mapToDomain);
    }

    @Override
    public Optional<Employee> findByCompanyIdAndUserId(String companyId, java.util.UUID userId) {
        return jpaRepository.findByCompanyIdAndUserId(companyId, userId).map(this::mapToDomain);
    }

    @Override
    public List<Employee> findAllByCompanyId(String companyId) {
        return jpaRepository.findAllByCompanyId(companyId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }
    
    private EmployeeEntity mapToEntity(Employee domain) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(domain.getId());
        entity.setCompanyId(domain.getCompanyId());
        entity.setRut(domain.getRut());
        entity.setUserId(domain.getUserId());
        entity.setFullName(domain.getFullName());
        entity.setBaseSalary(domain.getBaseSalary());
        entity.setAfp(domain.getAfp());
        entity.setHealthSystem(domain.getHealthSystem());
        entity.setHealthPercentage(domain.getHealthPercentage());
        entity.setFamilyLoads(domain.getFamilyLoads());
        return entity;
    }
    
    private Employee mapToDomain(EmployeeEntity entity) {
        return Employee.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .rut(entity.getRut())
                .userId(entity.getUserId())
                .fullName(entity.getFullName())
                .baseSalary(entity.getBaseSalary())
                .afp(entity.getAfp())
                .healthSystem(entity.getHealthSystem())
                .healthPercentage(entity.getHealthPercentage())
                .familyLoads(entity.getFamilyLoads())
                .build();
    }
}
