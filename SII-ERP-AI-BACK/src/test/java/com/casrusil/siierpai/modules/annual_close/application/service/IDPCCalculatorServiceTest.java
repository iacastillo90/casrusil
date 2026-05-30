package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.annual_close.domain.model.TaxRegime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IDPCCalculatorServiceTest {

    private IDPCCalculatorService service;

    @BeforeEach
    void setUp() {
        service = new IDPCCalculatorService();
    }

    @Test
    void calculateIDPC_ForSemiIntegrado_ShouldApply27Percent() {
        BigDecimal rli = new BigDecimal("10000000"); // 10 million
        BigDecimal result = service.calculateIDPC(rli, TaxRegime.REGIME_14A_SEMI_INTEGRADO);
        
        assertEquals(new BigDecimal("2700000.00"), result); // 2.7 million
    }

    @Test
    void calculateIDPC_ForProPymeTransparente_ShouldBeZero() {
        BigDecimal rli = new BigDecimal("10000000"); // 10 million
        BigDecimal result = service.calculateIDPC(rli, TaxRegime.REGIME_14D8_PROPYME_TRANSPARENTE);
        
        assertEquals(BigDecimal.ZERO, result);
    }
}
