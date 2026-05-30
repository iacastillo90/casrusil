package com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.sustainability.domain.port.out.GreenMetricsRepository;
import com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest.dto.GreenPassportDTO;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.swagger.v3.oas.annotations.tags.Tag; // Added import for @Tag
import org.slf4j.Logger; // Added import for Logger
import org.slf4j.LoggerFactory; // Added import for LoggerFactory

/**
 * 🌿 FERRARI ENGINE: API para métricas de sostenibilidad y "Pasaporte Verde".
 * 
 * <p>
 * Este controlador expone endpoints para que las empresas consulten su
 * impacto ambiental y obtengan su score verde para acceso a créditos CORFO.
 * 
 * @since 2.0 - Ferrari Engine Phase 2
 */
/**
 * REST Controller for Sustainability Metrics and Carbon Footprint.
 */
@RestController
@RequestMapping("/api/v1/sustainability/metrics")
@Tag(name = "Sustainability Metrics", description = "Carbon footprint tracking and environmental impact")
public class SustainabilityMetricsController {

    private static final Logger logger = LoggerFactory.getLogger(SustainabilityMetricsController.class);

    private final GreenMetricsRepository greenMetricsRepository;

    public SustainabilityMetricsController(GreenMetricsRepository greenMetricsRepository) {
        this.greenMetricsRepository = greenMetricsRepository;
    }

    /**
     * Obtiene el "Pasaporte Verde" de la empresa.
     * 
     * <p>
     * Combina salud financiera con salud ambiental para evaluación de riesgo.
     * 
     * <h2>Ejemplo de Respuesta:</h2>
     * 
     * <pre>
     * {
     *   "financialHealth": 85.5,
     *   "environmentalHealth": 42.3,
     *   "carbonFootprint": 1250.5,
     *   "greenSuppliers": 3,
     *   "riskLevel": "MEDIUM"
     * }
     * </pre>
     * 
     * @return Pasaporte Verde con métricas combinadas
     */
    @GetMapping("/score")
    public ResponseEntity<GreenPassportDTO> getGreenPassport() {
        CompanyId companyId = CompanyContext.requireCompanyId();

        // Calcular métricas ambientales
        BigDecimal carbonFootprint = greenMetricsRepository.calculateTotalCarbonFootprint(companyId);
        BigDecimal greenScore = greenMetricsRepository.calculateTotalGreenScore(companyId);
        long greenSuppliers = greenMetricsRepository.countGreenSuppliers(companyId);

        // Calcular salud ambiental (0-100)
        BigDecimal environmentalHealth = calculateEnvironmentalHealth(greenScore, carbonFootprint);

        // TODO: Calcular salud financiera (integrar con BalanceSheetService)
        BigDecimal financialHealth = new BigDecimal("75.0"); // Placeholder

        // Determinar nivel de riesgo
        String riskLevel = calculateRiskLevel(financialHealth, environmentalHealth);

        return ResponseEntity.ok(new GreenPassportDTO(
                financialHealth,
                environmentalHealth,
                carbonFootprint,
                greenSuppliers,
                riskLevel));
    }

    /**
     * Calcula la salud ambiental basándose en score verde y huella de carbono.
     * 
     * <p>
     * Fórmula: (greenScore / 10) - (carbonFootprint / 1000)
     * <p>
     * Normalizado a escala 0-100.
     */
    private BigDecimal calculateEnvironmentalHealth(BigDecimal greenScore, BigDecimal carbonFootprint) {
        // Componente positivo: score verde
        BigDecimal positiveComponent = greenScore.divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);

        // Componente negativo: huella de carbono
        BigDecimal negativeComponent = carbonFootprint.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);

        // Salud ambiental = positivo - negativo, normalizado a 0-100
        BigDecimal health = positiveComponent.subtract(negativeComponent).add(new BigDecimal("50"));

        // Clamp entre 0 y 100
        if (health.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (health.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100");
        }

        return health.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el nivel de riesgo combinado.
     * 
     * <p>
     * Criterios:
     * <ul>
     * <li>LOW: Ambas métricas > 70</li>
     * <li>MEDIUM: Al menos una métrica entre 40-70</li>
     * <li>HIGH: Alguna métrica < 40</li>
     * </ul>
     */
    private String calculateRiskLevel(BigDecimal financialHealth, BigDecimal environmentalHealth) {
        BigDecimal minHealth = financialHealth.min(environmentalHealth);

        if (minHealth.compareTo(new BigDecimal("70")) >= 0) {
            return "LOW";
        } else if (minHealth.compareTo(new BigDecimal("40")) >= 0) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
}
