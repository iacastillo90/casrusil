package com.casrusil.siierpai.modules.payroll.application.service;

import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import com.casrusil.siierpai.modules.payroll.domain.model.PayrollPeriod;
import com.casrusil.siierpai.modules.payroll.domain.port.EmployeeRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayrollPeriodRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayslipRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.TaxParameterProviderPort;
import com.casrusil.siierpai.modules.payroll.infrastructure.previred.PreviredFileGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Codificación del archivo Previred.
 *
 * <p>Previred exige un archivo de texto en codificación ANSI (Windows-1252) y
 * fin de línea CRLF. UTF-8 rompe los nombres con tildes (los envía como dos
 * bytes), por lo que el test verifica el byte real de "í".</p>
 */
class PayrollPreviredCharsetTest {

    private static final Charset ANSI = Charset.forName("windows-1252");

    @Test
    void previredFileUsesAnsiAndCrlfForAccentedNames() {
        EmployeeRepositoryPort employees = mock(EmployeeRepositoryPort.class);
        PayrollPeriodRepositoryPort periods = mock(PayrollPeriodRepositoryPort.class);
        PayslipRepositoryPort payslips = mock(PayslipRepositoryPort.class);
        TaxParameterProviderPort taxParameters = mock(TaxParameterProviderPort.class);

        PayrollServiceImpl service = new PayrollServiceImpl(
                employees, periods, payslips, mock(ApplicationEventPublisher.class),
                new PreviredFileGenerator(), taxParameters);

        Employee employee = Employee.builder()
                .id(1L)
                .companyId("comp-1")
                .rut("12345678-9")
                .fullName("María Fernández")
                .baseSalary(new BigDecimal("1000000"))
                .afp("Provida")
                .healthSystem("Fonasa")
                .build();
        PayrollPeriod period = PayrollPeriod.builder()
                .id(1L)
                .companyId("comp-1")
                .period(YearMonth.of(2026, 11))
                .ufValue(new BigDecimal("39731.79"))
                .utmValue(new BigDecimal("69751"))
                .build();

        when(periods.findByPeriodAndCompanyId(YearMonth.of(2026, 11), "comp-1")).thenReturn(Optional.of(period));
        when(employees.findAllByCompanyId("comp-1")).thenReturn(List.of(employee));
        when(payslips.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        byte[] file = service.generatePreviredFile(
                PayrollProcessRequest.builder().year(2026).month(11).build(), "comp-1");

        String ansiDecoded = new String(file, ANSI);
        assertTrue(ansiDecoded.contains("María"), "El nombre debe leerse en ANSI");
        assertFalse(ansiDecoded.contains("Ã"), "No debe contener la secuencia UTF-8 de la tilde");
        assertFalse(new String(file, StandardCharsets.UTF_8).contains("María"),
                "El archivo no debe estar en UTF-8");
        assertTrue(indexOf(file, (byte) 0x0D, (byte) 0x0A) >= 0, "Debe usar CRLF");
    }

    @Test
    void previredFileEmitsOneLinePerEmployeeWith105Fields() {
        EmployeeRepositoryPort employees = mock(EmployeeRepositoryPort.class);
        PayrollPeriodRepositoryPort periods = mock(PayrollPeriodRepositoryPort.class);
        PayslipRepositoryPort payslips = mock(PayslipRepositoryPort.class);

        PayrollServiceImpl service = new PayrollServiceImpl(
                employees, periods, payslips, mock(ApplicationEventPublisher.class),
                new PreviredFileGenerator(), mock(TaxParameterProviderPort.class));

        Employee employee = Employee.builder()
                .id(2L).companyId("comp-1").rut("11111111-1").fullName("Ana Perez")
                .baseSalary(new BigDecimal("900000")).afp("Modelo").healthSystem("Fonasa").build();
        PayrollPeriod period = PayrollPeriod.builder()
                .id(2L).companyId("comp-1").period(YearMonth.of(2026, 11))
                .ufValue(new BigDecimal("39731.79")).utmValue(new BigDecimal("69751")).build();

        when(periods.findByPeriodAndCompanyId(YearMonth.of(2026, 11), "comp-1")).thenReturn(Optional.of(period));
        when(employees.findAllByCompanyId("comp-1")).thenReturn(List.of(employee));
        when(payslips.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String content = new String(service.generatePreviredFile(
                PayrollProcessRequest.builder().year(2026).month(11).build(), "comp-1"), ANSI);

        String line = content.substring(0, content.indexOf("\r\n"));
        assertTrue(line.split(";", -1).length >= 105, "Cada línea debe tener al menos 105 campos");
    }

    private int indexOf(byte[] data, byte first, byte second) {
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i] == first && data[i + 1] == second) {
                return i;
            }
        }
        return -1;
    }
}
