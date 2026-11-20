package com.casrusil.siierpai.modules.payroll.infrastructure.web;

import com.casrusil.siierpai.modules.payroll.application.service.PayrollUseCase;
import com.casrusil.siierpai.modules.payroll.domain.dto.EmployeeResponse;
import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PayrollEmployeesEndpointTest {

    private final PayrollUseCase useCase = mock(PayrollUseCase.class);
    private final PayrollController controller = new PayrollController(useCase);

    @Test
    void getEmployeesShouldReturnFrontContract() throws Exception {
        CompanyId companyId = CompanyId.random();
        when(useCase.getEmployees(anyString())).thenReturn(List.of(
                new EmployeeResponse(1L, "Ana Pérez", "11111111-1", new BigDecimal("900000"), "Habitat", "Isapre")));

        var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(controller::getEmployees);

        assertEquals(200, response.getStatusCode().value());
        EmployeeResponse employee = response.getBody().get(0);
        assertEquals("Ana Pérez", employee.name());
        assertEquals(new BigDecimal("900000"), employee.base());
        assertEquals("Isapre", employee.isapre());
    }

    @Test
    void getMyPayslipsShouldUseAuthenticatedUserAndCompany() throws Exception {
        UserId userId = UserId.random();
        CompanyId companyId = CompanyId.random();
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userId, null, java.util.List.of()));

        try {
            when(useCase.getMyPayslips(companyId.value().toString(), userId.value()))
                    .thenReturn(java.util.List.of());

            var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(controller::getMyPayslips);

            assertEquals(200, response.getStatusCode().value());
            verify(useCase).getMyPayslips(companyId.value().toString(), userId.value());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void employeeResponseShouldMapDomainFields() {
        Employee employee = Employee.builder()
                .id(7L).rut("22222222-2").fullName("Juan Soto")
                .baseSalary(new BigDecimal("1000")).afp("Provida").healthSystem("Fonasa").build();

        EmployeeResponse response = EmployeeResponse.from(employee);

        assertEquals("Juan Soto", response.name());
        assertEquals(new BigDecimal("1000"), response.base());
        assertEquals("Fonasa", response.isapre());
    }
}
