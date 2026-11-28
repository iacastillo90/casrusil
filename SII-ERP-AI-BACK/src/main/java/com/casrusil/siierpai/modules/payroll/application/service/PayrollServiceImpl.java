package com.casrusil.siierpai.modules.payroll.application.service;

import com.casrusil.siierpai.modules.payroll.domain.dto.EmployeeResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollUseCase {

    private final EmployeeRepositoryPort employeeRepository;
    private final PayrollPeriodRepositoryPort periodRepository;
    private final PayslipRepositoryPort payslipRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PreviredFileGenerator previredFileGenerator;
    private final TaxParameterProviderPort taxParameterProvider;

    private static final BigDecimal AFP_RATE = new BigDecimal("0.1144"); // Average AFP rate approx 11.44%
    private static final BigDecimal HEALTH_RATE = new BigDecimal("0.07"); // 7% Fonasa standard
    private static final BigDecimal UNEMPLOYMENT_RATE = new BigDecimal("0.006"); // 0.6% AFC for permanent contracts

    @Override
    @Transactional
    public PayrollPeriodResponse openPeriod(OpenPayrollPeriodRequest request, String companyId) {
        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());

        java.util.Optional<PayrollPeriod> existing = periodRepository.findByPeriodAndCompanyId(ym, companyId);
        if (existing.isPresent()) {
            PayrollPeriod period = existing.get();
            if (period.isClosed()) {
                throw new IllegalStateException("El período " + ym + " ya está cerrado");
            }
            return PayrollPeriodResponse.from(period);
        }

        BigDecimal ufValue = request.getUfValue();
        BigDecimal utmValue = request.getUtmValue();
        if (ufValue == null || utmValue == null) {
            TaxParameterProviderPort.TaxParameters params = taxParameterProvider.findForYear(ym.getYear())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No hay parámetros tributarios (UF/UTM) para " + ym.getYear()
                                    + ". Envía ufValue y utmValue o carga los parámetros del año."));
            ufValue = ufValue != null ? ufValue : params.ufValue();
            utmValue = utmValue != null ? utmValue : params.utmValue();
        }

        PayrollPeriod period = PayrollPeriod.builder()
                .companyId(companyId)
                .period(ym)
                .ufValue(ufValue)
                .utmValue(utmValue)
                .isClosed(false)
                .build();

        return PayrollPeriodResponse.from(periodRepository.save(period));
    }

    @Override
    @Transactional
    public List<PayslipResponse> calculatePayrollForPeriod(PayrollProcessRequest request, String companyId) {
        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
        
        PayrollPeriod period = periodRepository.findByPeriodAndCompanyId(ym, companyId)
                .orElseThrow(() -> new com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException("Período de remuneraciones no encontrado"));

        if (period.isClosed()) {
            throw new RuntimeException("Payroll period is already closed");
        }

        List<Employee> employees = employeeRepository.findAllByCompanyId(companyId);

        List<Payslip> calculatedPayslips = employees.stream()
                .map(emp -> calculatePayslip(emp, period))
                .map(payslipRepository::save)
                .collect(Collectors.toList());

        List<PayslipResponse> responses = calculatedPayslips.stream()
                .map(payslip -> mapToResponse(payslip, findEmployee(employees, payslip), period))
                .collect(Collectors.toList());

        // --- EVENT PUBLICATION ---
        BigDecimal totalNet = calculatedPayslips.stream().map(Payslip::getNetSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTaxable = calculatedPayslips.stream().map(Payslip::getTaxableIncome).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        eventPublisher.publishEvent(new PayrollCalculatedEvent(
                companyId,
                period.getPeriod(),
                Collections.emptyMap(), // Can be expanded to group total AFP, Total Fonasa
                totalNet,
                totalTaxable
        ));
        
        return responses;
    }

    @Override
    public byte[] generatePreviredFile(PayrollProcessRequest request, String companyId) {
        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());

        PayrollPeriod period = periodRepository.findByPeriodAndCompanyId(ym, companyId)
                .orElseThrow(() -> new com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException("Período de remuneraciones no encontrado"));

        List<Employee> employees = employeeRepository.findAllByCompanyId(companyId);

        // Se genera una línea de 105 campos por trabajador con su liquidación del período.
        StringBuilder sb = new StringBuilder();
        for (Employee employee : employees) {
            Payslip payslip = calculatePayslip(employee, period);
            sb.append(previredFileGenerator.generateLine(employee, payslip, period));
            sb.append("\r\n"); // PreviRed usa fin de línea CRLF.
        }

        // PreviRed publica el archivo en formato de texto plano y su carga histórica
        // usa el juego de caracteres ANSI/Windows-1252 (compatible con ISO-8859-1
        // para los nombres usados en Chile). Se deja configurable por si Previred
        // cambia el requisito: payroll.previred.charset.
        return sb.toString().getBytes(previredCharset());
    }

    private static final String DEFAULT_PREVIRED_CHARSET = "windows-1252";

    @org.springframework.beans.factory.annotation.Value("${payroll.previred.charset:" + DEFAULT_PREVIRED_CHARSET + "}")
    private String previredCharsetName;

    private java.nio.charset.Charset previredCharset() {
        try {
            return java.nio.charset.Charset.forName(previredCharsetName);
        } catch (Exception invalidCharset) {
            return java.nio.charset.Charset.forName(DEFAULT_PREVIRED_CHARSET);
        }
    }

    @Override
    public List<EmployeeResponse> getEmployees(String companyId) {
        return employeeRepository.findAllByCompanyId(companyId).stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    public List<PayslipResponse> getMyPayslips(String companyId, java.util.UUID userId) {
        com.casrusil.siierpai.modules.payroll.domain.model.Employee employee = employeeRepository
                .findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException(
                        "No hay un empleado vinculado al usuario autenticado"));
        java.util.Map<Long, PayrollPeriod> periodsById = new java.util.HashMap<>();
        return payslipRepository.findByEmployeeId(convertToUUID(employee.getId()).toString()).stream()
                .map(payslip -> {
                    PayrollPeriod period = payslip.getPeriodId() == null ? null
                            : periodsById.computeIfAbsent(payslip.getPeriodId(),
                                    periodId -> periodRepository.findByIdAndCompanyId(periodId, companyId).orElse(null));
                    return mapToResponse(payslip, employee, period);
                })
                .collect(Collectors.toList());
    }

    private Payslip calculatePayslip(Employee employee, PayrollPeriod period) {
        BigDecimal baseSalary = employee.getBaseSalary();
        
        // Simplified Gratification (25% of base salary capped at 4.75 IMM)
        BigDecimal gratification = baseSalary.multiply(new BigDecimal("0.25"));
        BigDecimal taxableIncome = baseSalary.add(gratification);
        
        // Deductions
        BigDecimal afpDiscount = taxableIncome.multiply(AFP_RATE).setScale(0, RoundingMode.HALF_UP);
        
        BigDecimal healthDiscount = employee.getHealthPercentage() != null ?
                employee.getHealthPercentage().multiply(period.getUfValue()).setScale(0, RoundingMode.HALF_UP) :
                taxableIncome.multiply(HEALTH_RATE).setScale(0, RoundingMode.HALF_UP);

        BigDecimal afcDiscount = taxableIncome.multiply(UNEMPLOYMENT_RATE).setScale(0, RoundingMode.HALF_UP);
        
        // Simplified tax calculation (Impuesto Único de Segunda Categoría)
        BigDecimal singleTax = calculateSecondCategoryTax(taxableIncome.subtract(afpDiscount).subtract(healthDiscount).subtract(afcDiscount), period.getUtmValue());
        
        BigDecimal totalDiscounts = afpDiscount.add(healthDiscount).add(afcDiscount).add(singleTax);
        BigDecimal netSalary = taxableIncome.subtract(totalDiscounts);

        return Payslip.builder()
                .companyId(employee.getCompanyId())
                .employeeId(convertToUUID(employee.getId()))
                .periodId(period.getId())
                .baseSalary(baseSalary)
                .gratification(gratification)
                .taxableIncome(taxableIncome)
                .nonTaxableIncome(BigDecimal.ZERO)
                .afpDiscount(afpDiscount)
                .healthDiscount(healthDiscount)
                .unemploymentInsurance(afcDiscount)
                .singleTax(singleTax)
                .totalDiscounts(totalDiscounts)
                .netSalary(netSalary)
                .build();
    }
    
    /**
     * Tramos del Impuesto Único de Segunda Categoría (IUSC), mensual, expresados en UTM.
     * Cada tramo: [límite inferior UTM, límite superior UTM, factor, cantidad a rebajar en UTM].
     * El último tramo usa {@code null} como límite superior (sin tope).
     *
     * <p>Fórmula: {@code impuesto = baseTributable * factor - (rebajaUTM * valorUTM)}.
     * Los factores y rebajas son los definidos por el SII para el IUSC.
     */
    private static final BigDecimal[][] IUSC_BRACKETS_UTM = {
            // desde,  hasta,   factor,   rebaja(UTM)
            { new BigDecimal("0"),    new BigDecimal("13.5"), new BigDecimal("0.0"),   new BigDecimal("0.0") },
            { new BigDecimal("13.5"), new BigDecimal("30"),   new BigDecimal("0.04"),  new BigDecimal("0.54") },
            { new BigDecimal("30"),   new BigDecimal("50"),   new BigDecimal("0.08"),  new BigDecimal("1.74") },
            { new BigDecimal("50"),   new BigDecimal("70"),   new BigDecimal("0.135"), new BigDecimal("4.49") },
            { new BigDecimal("70"),   new BigDecimal("90"),   new BigDecimal("0.23"),  new BigDecimal("11.14") },
            { new BigDecimal("90"),   new BigDecimal("120"),  new BigDecimal("0.304"), new BigDecimal("17.80") },
            { new BigDecimal("120"),  new BigDecimal("310"),  new BigDecimal("0.35"),  new BigDecimal("23.32") },
            { new BigDecimal("310"),  null,                   new BigDecimal("0.40"),  new BigDecimal("38.82") },
    };

    /**
     * Calcula el Impuesto Único de Segunda Categoría aplicando la tabla oficial de
     * tramos del SII (en UTM). Reemplaza el cálculo "dummy" anterior.
     *
     * @param baseTributable base tributable mensual (renta imponible menos cotizaciones), en pesos
     * @param utmValue       valor de la UTM del período, en pesos
     * @return impuesto a retener, en pesos (nunca negativo)
     */
    private BigDecimal calculateSecondCategoryTax(BigDecimal baseTributable, BigDecimal utmValue) {
        if (baseTributable == null || utmValue == null || baseTributable.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        for (BigDecimal[] bracket : IUSC_BRACKETS_UTM) {
            BigDecimal lowerPesos = bracket[0].multiply(utmValue);
            BigDecimal upperPesos = (bracket[1] != null) ? bracket[1].multiply(utmValue) : null;

            boolean inBracket = baseTributable.compareTo(lowerPesos) > 0
                    && (upperPesos == null || baseTributable.compareTo(upperPesos) <= 0);
            if (inBracket) {
                BigDecimal factor = bracket[2];
                BigDecimal rebajaPesos = bracket[3].multiply(utmValue);
                BigDecimal tax = baseTributable.multiply(factor).subtract(rebajaPesos);
                return tax.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Converts a Long employee ID to a UUID for consistency with the payslip model.
     * Uses name-based UUID generation from the employee ID string representation.
     */
    private UUID convertToUUID(Long employeeId) {
        return UUID.nameUUIDFromBytes(employeeId.toString().getBytes());
    }

    /** Empleado al que pertenece una liquidación (por id de nómina). */
    private Employee findEmployee(List<Employee> employees, Payslip payslip) {
        return employees.stream()
                .filter(employee -> convertToUUID(employee.getId()).equals(payslip.getEmployeeId()))
                .findFirst()
                .orElse(null);
    }

    private PayslipResponse mapToResponse(Payslip payslip, Employee employee, PayrollPeriod period) {
        return PayslipResponse.builder()
                .id(payslip.getId())
                .employeeName(employee != null ? employee.getFullName() : null)
                .rut(employee != null ? employee.getRut() : null)
                .period(period != null && period.getPeriod() != null ? period.getPeriod().toString() : null)
                .taxableIncome(payslip.getTaxableIncome())
                .nonTaxableIncome(payslip.getNonTaxableIncome())
                .totalDiscounts(payslip.getTotalDiscounts())
                .netSalary(payslip.getNetSalary())
                .build();
    }
}
