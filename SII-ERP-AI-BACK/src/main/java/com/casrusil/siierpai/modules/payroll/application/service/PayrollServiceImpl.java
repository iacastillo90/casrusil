package com.casrusil.siierpai.modules.payroll.application.service;

import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayslipResponse;
import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import com.casrusil.siierpai.modules.payroll.domain.model.PayrollPeriod;
import com.casrusil.siierpai.modules.payroll.domain.model.Payslip;
import com.casrusil.siierpai.modules.payroll.domain.port.EmployeeRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayrollPeriodRepositoryPort;
import com.casrusil.siierpai.modules.payroll.domain.port.PayslipRepositoryPort;
import com.casrusil.siierpai.shared.domain.event.PayrollCalculatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollUseCase {

    private final EmployeeRepositoryPort employeeRepository;
    private final PayrollPeriodRepositoryPort periodRepository;
    private final PayslipRepositoryPort payslipRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final BigDecimal AFP_RATE = new BigDecimal("0.1144"); // Average AFP rate approx 11.44%
    private static final BigDecimal HEALTH_RATE = new BigDecimal("0.07"); // 7% Fonasa standard
    private static final BigDecimal UNEMPLOYMENT_RATE = new BigDecimal("0.006"); // 0.6% AFC for permanent contracts

    @Override
    @Transactional
    public List<PayslipResponse> calculatePayrollForPeriod(PayrollProcessRequest request, String companyId) {
        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
        
        PayrollPeriod period = periodRepository.findByPeriodAndCompanyId(ym, companyId)
                .orElseThrow(() -> new RuntimeException("Payroll period not configured or not found"));

        if (period.isClosed()) {
            throw new RuntimeException("Payroll period is already closed");
        }

        List<Employee> employees = employeeRepository.findAllByCompanyId(companyId);

        List<Payslip> calculatedPayslips = employees.stream()
                .map(emp -> calculatePayslip(emp, period))
                .map(payslipRepository::save)
                .collect(Collectors.toList());

        List<PayslipResponse> responses = calculatedPayslips.stream()
                .map(this::mapToResponse)
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
        // Mock generation of 105-column flat text file for Previred integration
        String mockTxtContent = "01;88888888-8;EMP NAME;105_FIELDS_MOCKED...\n";
        return mockTxtContent.getBytes();
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
                .employeeId(employee.getId())
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
    
    private BigDecimal calculateSecondCategoryTax(BigDecimal baseTributable, BigDecimal utmValue) {
        // Mocked tranches calculation based on UTM
        // In a real implementation this would iterate over the SII tax brackets
        if (baseTributable.compareTo(utmValue.multiply(new BigDecimal("13.5"))) <= 0) {
            return BigDecimal.ZERO; // Exempt
        }
        // Dummy 4% calculation for tests
        return baseTributable.multiply(new BigDecimal("0.04")).setScale(0, RoundingMode.HALF_UP);
    }

    private PayslipResponse mapToResponse(Payslip payslip) {
        return PayslipResponse.builder()
                .id(payslip.getId())
                .taxableIncome(payslip.getTaxableIncome())
                .nonTaxableIncome(payslip.getNonTaxableIncome())
                .totalDiscounts(payslip.getTotalDiscounts())
                .netSalary(payslip.getNetSalary())
                .build();
    }
}
