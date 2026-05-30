package com.casrusil.siierpai.modules.payroll.domain.port;

import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepositoryPort {
    Employee save(Employee employee);
    Optional<Employee> findByIdAndCompanyId(Long id, String companyId);
    List<Employee> findAllByCompanyId(String companyId);
}
