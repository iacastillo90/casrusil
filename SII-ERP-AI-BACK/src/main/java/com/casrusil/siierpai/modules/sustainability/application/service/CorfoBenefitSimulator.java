package com.casrusil.siierpai.modules.sustainability.application.service;

import com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest.dto.GreenPassportDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CorfoBenefitSimulator {

    public CorfoBenefitSimulation simulate(GreenPassportDTO passport) {
        // Lógica de negocio simulada para beneficios CORFO
        
        BigDecimal environmentalHealth = passport.environmentalHealth();
        
        // Beneficios base
        BigDecimal taxRebatePercentage = BigDecimal.ZERO;
        BigDecimal preferentialCreditRate = new BigDecimal("4.5"); // Tasa comercial estándar
        String eligibilityStatus = "NO_ELIGIBLE";
        
        if (environmentalHealth.compareTo(new BigDecimal("80")) >= 0) {
            taxRebatePercentage = new BigDecimal("15.0"); // 15% rebaja fiscal en activos verdes
            preferentialCreditRate = new BigDecimal("1.5"); // 1.5% tasa preferencial
            eligibilityStatus = "GOLD_ELIGIBLE";
        } else if (environmentalHealth.compareTo(new BigDecimal("50")) >= 0) {
            taxRebatePercentage = new BigDecimal("5.0"); 
            preferentialCreditRate = new BigDecimal("3.0");
            eligibilityStatus = "SILVER_ELIGIBLE";
        }

        return new CorfoBenefitSimulation(
            eligibilityStatus,
            taxRebatePercentage,
            preferentialCreditRate,
            "Para acceder al siguiente nivel (GOLD), reduce tu huella de carbono en un 20%."
        );
    }

    public record CorfoBenefitSimulation(
        String eligibilityStatus,
        BigDecimal taxRebatePercentage,
        BigDecimal preferentialCreditRate,
        String nextLevelRequirement
    ) {}
}
