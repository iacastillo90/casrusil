package com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

/**
 * 🌿 DTO para el "Pasaporte Verde" de una empresa.
 * 
 * <p>
 * Combina salud financiera con salud ambiental para evaluación
 * de riesgo climático por parte de bancos y CORFO.
 * 
 * @param financialHealth     Score de salud financiera (0-100)
 * @param environmentalHealth Score de salud ambiental (0-100)
 * @param carbonFootprint     Huella de carbono total en kg CO2e
 * @param greenSuppliers      Número de proveedores con certificación verde
 * @param riskLevel           Nivel de riesgo combinado (LOW, MEDIUM, HIGH)
 * @since 2.0 - Ferrari Engine Phase 2
 */
public record GreenPassportDTO(
        BigDecimal financialHealth,
        BigDecimal environmentalHealth,
        BigDecimal carbonFootprint,
        long greenSuppliers,
        String riskLevel) {
}
