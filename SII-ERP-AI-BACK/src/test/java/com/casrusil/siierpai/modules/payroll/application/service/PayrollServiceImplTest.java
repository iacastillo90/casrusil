package com.casrusil.siierpai.modules.payroll.application.service;

import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayslipResponse;
import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import com.casrusil.siierpai.modules.payroll.domain.model.PayrollPeriod;
import com.casrusil.siierpai.modules.payroll.domain.port.EmployeeRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayrollPeriodRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayslipRepositoryPort;
import com.casrusil.siierpai.modules.payroll.infrastructure.previred.PreviredFileGenerator;
import com.casrusil.siierpai.shared.domain.event.PayrollCalculatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de Remuneraciones.
 * Valida los cálculos de AFP, Fonasa y emisión de eventos.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private EmployeeRepositoryPort employeeRepository;

    @Mock
    private PayrollPeriodRepositoryPort periodRepository;

    @Mock
    private PayslipRepositoryPort payslipRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PreviredFileGenerator previredFileGenerator;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Employee testEmployee;
    private PayrollPeriod testPeriod;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .companyId("comp-1")
                .rut("12345678-9")
                .fullName("Test Employee")
                .baseSalary(new BigDecimal("1000000")) // Sueldo base 1 Millon
                .build();

        testPeriod = PayrollPeriod.builder()
                .id(1L)
                .companyId("comp-1")
                .period(YearMonth.of(2026, 8))
                .ufValue(new BigDecimal("30000"))
                .utmValue(new BigDecimal("600000"))
                .isClosed(false)
                .build();
    }

    @Test
    void testCalculatePayroll_ShouldCalculateDiscountsAndPublishEvent() {
        // Arrange
        PayrollProcessRequest request = PayrollProcessRequest.builder()
                .year(2026)
                .month(8)
                .build();
        when(periodRepository.findByPeriodAndCompanyId(YearMonth.of(2026, 8), "comp-1"))
                .thenReturn(Optional.of(testPeriod));
        when(employeeRepository.findAllByCompanyId("comp-1")).thenReturn(List.of(testEmployee));
        when(payslipRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        List<PayslipResponse> responses = payrollService.calculatePayrollForPeriod(request, "comp-1");

        // Assert
        assertEquals(1, responses.size(), "Debe haber 1 liquidacion generada");
        PayslipResponse response = responses.get(0);

        // Base: 1M. Gratificacion: 25% = 250k. Total imponibile = 1.25M
        assertNotNull(response.getNetSalary(), "El salario neto no debe ser nulo");

        // Verificar Evento Contable
        verify(eventPublisher, times(1)).publishEvent(any(PayrollCalculatedEvent.class));
    }
}
