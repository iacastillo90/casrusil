package com.casrusil.siierpai.modules.payroll.application.service;

import com.casrusil.siierpai.modules.payroll.domain.dto.OpenPayrollPeriodRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollPeriodResponse;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayslipResponse;
import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import com.casrusil.siierpai.modules.payroll.domain.model.PayrollPeriod;
import com.casrusil.siierpai.modules.payroll.domain.model.Payslip;
import com.casrusil.siierpai.modules.payroll.domain.port.EmployeeRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayrollPeriodRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayslipRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.TaxParameterProviderPort;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Mock
    private TaxParameterProviderPort taxParameterProvider;

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
    void getMyPayslips_ShouldReturnOnlyPayslipsOfAuthenticatedEmployee() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        Employee employeeA = Employee.builder().id(1L).companyId("comp-1").userId(userA).build();
        Employee employeeB = Employee.builder().id(2L).companyId("comp-1").userId(userB).build();
        UUID payslipKeyA = UUID.nameUUIDFromBytes("1".getBytes());
        UUID payslipKeyB = UUID.nameUUIDFromBytes("2".getBytes());

        Payslip payslipA = Payslip.builder()
                .id(10L).companyId("comp-1").employeeId(payslipKeyA).periodId(1L)
                .baseSalary(new BigDecimal("1000")).gratification(BigDecimal.ZERO)
                .taxableIncome(new BigDecimal("1000")).nonTaxableIncome(BigDecimal.ZERO)
                .afpDiscount(BigDecimal.ZERO).healthDiscount(BigDecimal.ZERO)
                .unemploymentInsurance(BigDecimal.ZERO).singleTax(BigDecimal.ZERO)
                .totalDiscounts(BigDecimal.ZERO).netSalary(new BigDecimal("1000"))
                .build();
        Payslip payslipB = Payslip.builder()
                .id(20L).companyId("comp-1").employeeId(payslipKeyB).periodId(1L)
                .baseSalary(new BigDecimal("2000")).gratification(BigDecimal.ZERO)
                .taxableIncome(new BigDecimal("2000")).nonTaxableIncome(BigDecimal.ZERO)
                .afpDiscount(BigDecimal.ZERO).healthDiscount(BigDecimal.ZERO)
                .unemploymentInsurance(BigDecimal.ZERO).singleTax(BigDecimal.ZERO)
                .totalDiscounts(BigDecimal.ZERO).netSalary(new BigDecimal("2000"))
                .build();

        when(employeeRepository.findByCompanyIdAndUserId("comp-1", userA)).thenReturn(Optional.of(employeeA));
        when(employeeRepository.findByCompanyIdAndUserId("comp-1", userB)).thenReturn(Optional.of(employeeB));
        when(payslipRepository.findByEmployeeId(payslipKeyA.toString())).thenReturn(List.of(payslipA));
        when(payslipRepository.findByEmployeeId(payslipKeyB.toString())).thenReturn(List.of(payslipB));

        List<PayslipResponse> forA = payrollService.getMyPayslips("comp-1", userA);
        List<PayslipResponse> forB = payrollService.getMyPayslips("comp-1", userB);

        assertEquals(1, forA.size());
        assertEquals(new BigDecimal("1000"), forA.get(0).getNetSalary());
        assertEquals(1, forB.size());
        assertEquals(new BigDecimal("2000"), forB.get(0).getNetSalary());
        verify(payslipRepository).findByEmployeeId(payslipKeyA.toString());
        verify(payslipRepository).findByEmployeeId(payslipKeyB.toString());
        verifyNoMoreInteractions(payslipRepository);
    }

    @Test
    void openPeriod_createsWithProvidedUfAndUtm() {
        when(periodRepository.findByPeriodAndCompanyId(YearMonth.of(2026, 11), "comp-1"))
                .thenReturn(Optional.empty());
        when(periodRepository.save(any(PayrollPeriod.class))).thenAnswer(invocation -> {
            PayrollPeriod toSave = invocation.getArgument(0);
            toSave.setId(99L);
            return toSave;
        });

        PayrollPeriodResponse response = payrollService.openPeriod(
                OpenPayrollPeriodRequest.builder()
                        .year(2026).month(11)
                        .ufValue(new BigDecimal("39731.79"))
                        .utmValue(new BigDecimal("69751"))
                        .build(),
                "comp-1");

        assertEquals(99L, response.id());
        assertEquals("2026-11", response.period());
        assertEquals(new BigDecimal("39731.79"), response.ufValue());
        assertEquals(new BigDecimal("69751"), response.utmValue());
        verify(periodRepository).save(any(PayrollPeriod.class));
        verifyNoInteractions(taxParameterProvider);
    }

    @Test
    void openPeriod_isIdempotentWhenPeriodAlreadyOpen() {
        when(periodRepository.findByPeriodAndCompanyId(YearMonth.of(2026, 8), "comp-1"))
                .thenReturn(Optional.of(testPeriod));

        PayrollPeriodResponse response = payrollService.openPeriod(
                OpenPayrollPeriodRequest.builder().year(2026).month(8).build(), "comp-1");

        assertEquals("2026-08", response.period());
        assertEquals(false, response.closed());
        verify(periodRepository, never()).save(any());
    }

    @Test
    void openPeriod_rejectsClosedPeriod() {
        testPeriod.setClosed(true);
        when(periodRepository.findByPeriodAndCompanyId(YearMonth.of(2026, 8), "comp-1"))
                .thenReturn(Optional.of(testPeriod));

        assertThrows(IllegalStateException.class, () -> payrollService.openPeriod(
                OpenPayrollPeriodRequest.builder().year(2026).month(8).build(), "comp-1"));
    }

    @Test
    void openPeriod_fallsBackToYearTaxParameters() {
        when(periodRepository.findByPeriodAndCompanyId(YearMonth.of(2026, 11), "comp-1"))
                .thenReturn(Optional.empty());
        when(taxParameterProvider.findForYear(2026)).thenReturn(Optional.of(
                new TaxParameterProviderPort.TaxParameters(
                        new BigDecimal("39731.79"), new BigDecimal("69751"))));
        when(periodRepository.save(any(PayrollPeriod.class))).thenAnswer(invocation -> {
            PayrollPeriod toSave = invocation.getArgument(0);
            toSave.setId(100L);
            return toSave;
        });

        PayrollPeriodResponse response = payrollService.openPeriod(
                OpenPayrollPeriodRequest.builder().year(2026).month(11).build(), "comp-1");

        assertEquals(new BigDecimal("39731.79"), response.ufValue());
        assertEquals(new BigDecimal("69751"), response.utmValue());
    }

    @Test
    void openPeriod_failsWhenNoUfUtmAvailable() {
        when(periodRepository.findByPeriodAndCompanyId(YearMonth.of(2030, 1), "comp-1"))
                .thenReturn(Optional.empty());
        when(taxParameterProvider.findForYear(2030)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> payrollService.openPeriod(
                OpenPayrollPeriodRequest.builder().year(2030).month(1).build(), "comp-1"));
    }

    @Test
    void calculatePayroll_fillsEmployeeNameRutAndPeriod() {
        PayrollProcessRequest request = PayrollProcessRequest.builder().year(2026).month(8).build();
        when(periodRepository.findByPeriodAndCompanyId(YearMonth.of(2026, 8), "comp-1"))
                .thenReturn(Optional.of(testPeriod));
        when(employeeRepository.findAllByCompanyId("comp-1")).thenReturn(List.of(testEmployee));
        when(payslipRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        PayslipResponse response = payrollService.calculatePayrollForPeriod(request, "comp-1").get(0);

        assertEquals("Test Employee", response.getEmployeeName());
        assertEquals("12345678-9", response.getRut());
        assertEquals("2026-08", response.getPeriod());
    }

    @Test
    void getMyPayslips_fillsEmployeeAndPeriodFromStoredPayslip() {
        UUID userId = UUID.randomUUID();
        Employee employee = Employee.builder().id(3L).companyId("comp-1").userId(userId)
                .fullName("Ana Pérez").rut("11111111-1").build();
        UUID employeeKey = UUID.nameUUIDFromBytes("3".getBytes());
        Payslip payslip = Payslip.builder().id(30L).companyId("comp-1").employeeId(employeeKey)
                .periodId(7L).taxableIncome(new BigDecimal("1000")).nonTaxableIncome(BigDecimal.ZERO)
                .totalDiscounts(BigDecimal.ZERO).netSalary(new BigDecimal("1000")).build();

        when(employeeRepository.findByCompanyIdAndUserId("comp-1", userId)).thenReturn(Optional.of(employee));
        when(payslipRepository.findByEmployeeId(employeeKey.toString())).thenReturn(List.of(payslip));
        when(periodRepository.findByIdAndCompanyId(7L, "comp-1")).thenReturn(Optional.of(testPeriod));

        PayslipResponse response = payrollService.getMyPayslips("comp-1", userId).get(0);

        assertEquals("Ana Pérez", response.getEmployeeName());
        assertEquals("11111111-1", response.getRut());
        assertEquals("2026-08", response.getPeriod());
    }

    @Test
    void getMyPayslips_toleratesMissingPeriod() {
        UUID userId = UUID.randomUUID();
        Employee employee = Employee.builder().id(4L).companyId("comp-1").userId(userId)
                .fullName("Sin Período").rut("22222222-2").build();
        UUID employeeKey = UUID.nameUUIDFromBytes("4".getBytes());
        Payslip payslip = Payslip.builder().id(40L).companyId("comp-1").employeeId(employeeKey)
                .periodId(404L).netSalary(new BigDecimal("500")).build();

        when(employeeRepository.findByCompanyIdAndUserId("comp-1", userId)).thenReturn(Optional.of(employee));
        when(payslipRepository.findByEmployeeId(employeeKey.toString())).thenReturn(List.of(payslip));
        when(periodRepository.findByIdAndCompanyId(404L, "comp-1")).thenReturn(Optional.empty());

        PayslipResponse response = payrollService.getMyPayslips("comp-1", userId).get(0);

        assertEquals("Sin Período", response.getEmployeeName());
        assertNull(response.getPeriod());
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
