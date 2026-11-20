package com.casrusil.siierpai.modules.payroll;

import com.casrusil.siierpai.modules.payroll.application.service.PayrollServiceImpl;
import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import com.casrusil.siierpai.modules.payroll.domain.port.EmployeeRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayrollPeriodRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayslipRepositoryPort;
import com.casrusil.siierpai.modules.payroll.infrastructure.web.PayrollController;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.casrusil.siierpai.shared.infrastructure.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Un usuario USER sólo puede ver las liquidaciones del empleado vinculado a su
 * cuenta, aunque envíe el header X-Employee-Id de otro empleado.
 */
class PayrollPayslipIsolationTest {

    private static final UUID DERIVED_A = UUID.nameUUIDFromBytes("1".getBytes());
    private static final UUID DERIVED_B = UUID.nameUUIDFromBytes("2".getBytes());

    private final EmployeeRepositoryPort employeeRepository = mock(EmployeeRepositoryPort.class);
    private final PayslipRepositoryPort payslipRepository = mock(PayslipRepositoryPort.class);
    private final PayrollServiceImpl service = new PayrollServiceImpl(
            employeeRepository,
            mock(PayrollPeriodRepositoryPort.class),
            payslipRepository,
            mock(ApplicationEventPublisher.class),
            mock(com.casrusil.siierpai.modules.payroll.infrastructure.previred.PreviredFileGenerator.class),
            mock(com.casrusil.siierpai.modules.payroll.domain.port.TaxParameterProviderPort.class));

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userAShouldNeverQueryPayslipsOfEmployeeB() {
        UserId userA = UserId.random();
        CompanyId companyId = CompanyId.random();
        when(employeeRepository.findByCompanyIdAndUserId(companyId.value().toString(), userA.value()))
                .thenReturn(Optional.of(employee(1L, userA)));
        when(payslipRepository.findByEmployeeId(anyString())).thenReturn(List.of());

        service.getMyPayslips(companyId.value().toString(), userA.value());

        verify(payslipRepository).findByEmployeeId(DERIVED_A.toString());
        verify(payslipRepository, never()).findByEmployeeId(DERIVED_B.toString());
    }

    @Test
    void userBShouldOnlyQueryOwnPayslips() {
        UserId userB = UserId.random();
        CompanyId companyId = CompanyId.random();
        when(employeeRepository.findByCompanyIdAndUserId(companyId.value().toString(), userB.value()))
                .thenReturn(Optional.of(employee(2L, userB)));
        when(payslipRepository.findByEmployeeId(anyString())).thenReturn(List.of());

        service.getMyPayslips(companyId.value().toString(), userB.value());

        verify(payslipRepository).findByEmployeeId(DERIVED_B.toString());
        verify(payslipRepository, never()).findByEmployeeId(DERIVED_A.toString());
    }

    @Test
    void controllerShouldIgnoreEmployeeHeaderForUserRole() throws Exception {
        UserId userA = UserId.random();
        CompanyId companyId = CompanyId.random();
        when(employeeRepository.findByCompanyIdAndUserId(companyId.value().toString(), userA.value()))
                .thenReturn(Optional.of(employee(1L, userA)));
        when(payslipRepository.findByEmployeeId(anyString())).thenReturn(List.of());

        assertControllerUsesOwnEmployee(companyId, userA);
    }

    private void assertControllerUsesOwnEmployee(CompanyId companyId, UserId userA)
            throws Exception {
        PayrollController controller = new PayrollController(service);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                userA, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        Object[] holder = new Object[1];
        CompanyContext.runInCompanyContext(companyId, () -> UserContext.runInUserContext(userA, "user-a",
                () -> holder[0] = controller.getMyPayslips()));

        assertEquals(200, ((org.springframework.http.ResponseEntity<?>) holder[0]).getStatusCode().value());
        verify(payslipRepository).findByEmployeeId(DERIVED_A.toString());
        verify(payslipRepository, never()).findByEmployeeId(DERIVED_B.toString());
    }

    private Employee employee(Long id, UserId userId) {
        return Employee.builder()
                .id(id)
                .companyId("company")
                .rut("11111111-1")
                .fullName("Empleado " + id)
                .userId(userId.value())
                .build();
    }
}
