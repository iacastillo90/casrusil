package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.annual_close.domain.model.TaxAdjustment;
import com.casrusil.siierpai.modules.annual_close.domain.port.TaxAdjustmentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RLICalculatorServiceTest {

    @Mock
    private TaxAdjustmentRepositoryPort taxAdjustmentRepository;

    @InjectMocks
    private RLICalculatorService rliCalculatorService;

    @Test
    void calculateRLI_ShouldApplyAdditionsAndDeductionsCorrectly() {
        // Arrange
        String companyId = "c1";
        int fiscalYear = 2026;
        BigDecimal baseProfit = new BigDecimal("10000000"); // 10 million

        List<TaxAdjustment> adjustments = Arrays.asList(
                TaxAdjustment.builder().type("ADDITION").amount(new BigDecimal("2000000")).build(), // Rejected expense
                TaxAdjustment.builder().type("DEDUCTION").amount(new BigDecimal("500000")).build()   // Exempt income
        );

        when(taxAdjustmentRepository.findByCompanyIdAndFiscalYear(companyId, fiscalYear))
                .thenReturn(adjustments);

        // Act
        BigDecimal result = rliCalculatorService.calculateRLI(baseProfit, companyId, fiscalYear);

        // Assert: 10M + 2M - 0.5M = 11.5M
        assertEquals(new BigDecimal("11500000"), result);
    }
}
