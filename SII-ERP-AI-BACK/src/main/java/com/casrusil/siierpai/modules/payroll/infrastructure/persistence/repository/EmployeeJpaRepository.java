package com.casrusil.siierpai.modules.payroll.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeJpaRepository extends JpaRepository<EmployeeEntity, Long> {
    Optional<EmployeeEntity> findByIdAndCompanyId(Long id, String companyId);
    List<EmployeeEntity> findAllByCompanyId(String companyId);

    java.util.Optional<EmployeeEntity> findByCompanyIdAndUserId(String companyId, java.util.UUID userId);
}
