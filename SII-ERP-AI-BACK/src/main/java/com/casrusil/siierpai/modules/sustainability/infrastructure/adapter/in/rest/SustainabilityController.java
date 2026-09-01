package com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.sustainability.domain.port.out.GreenMetricsRepository;
import com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest.dto.GreenPassportDTO;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 🌿 FERRARI ENGINE: API para métricas de sostenibilidad y "Pasaporte Verde".
 * 
 * <p>
 * Este controlador expone endpoints para que las empresas consulten su
 * impacto ambiental y obtengan su score verde para acceso a créditos CORFO.
 * 
 * @since 2.0 - Ferrari Engine Phase 2
 */
import com.casrusil.siierpai.modules.sustainability.application.service.CorfoBenefitSimulator;
import com.casrusil.siierpai.modules.sustainability.application.service.EsgReportService;
import com.casrusil.siierpai.modules.accounting.domain.service.FinancialHealthService;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sustainability")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS','AUDITOR')")
public class SustainabilityController {

    private final GreenMetricsRepository greenMetricsRepository;
    private final EsgReportService esgReportService;
    private final CorfoBenefitSimulator corfoBenefitSimulator;
    private final FinancialHealthService financialHealthService;

    public SustainabilityController(GreenMetricsRepository greenMetricsRepository, 
                                    EsgReportService esgReportService, 
                                    CorfoBenefitSimulator corfoBenefitSimulator,
                                    FinancialHealthService financialHealthService) {
        this.greenMetricsRepository = greenMetricsRepository;
        this.esgReportService = esgReportService;
        this.corfoBenefitSimulator = corfoBenefitSimulator;
        this.financialHealthService = financialHealthService;
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

        // Salud financiera real, derivada del Balance General a la fecha.
        BigDecimal financialHealth = financialHealthService.calculate(companyId, LocalDate.now());

        // Determinar nivel de riesgo
        String riskLevel = calculateRiskLevel(financialHealth, environmentalHealth);

        return ResponseEntity.ok(new GreenPassportDTO(
                financialHealth,
                environmentalHealth,
                carbonFootprint,
                greenSuppliers,
                riskLevel));
    }

    @GetMapping("/esg-report")
    public ResponseEntity<EsgReportService.EsgReport> getEsgReport() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(esgReportService.generateReport(companyId));
    }

    @GetMapping("/corfo-simulator")
    public ResponseEntity<CorfoBenefitSimulator.CorfoBenefitSimulation> simulateCorfoBenefits() {
        // We get the passport first to simulate the benefits
        CompanyId companyId = CompanyContext.requireCompanyId();
        
        BigDecimal carbonFootprint = greenMetricsRepository.calculateTotalCarbonFootprint(companyId);
        BigDecimal greenScore = greenMetricsRepository.calculateTotalGreenScore(companyId);
        long greenSuppliers = greenMetricsRepository.countGreenSuppliers(companyId);
        
        BigDecimal environmentalHealth = calculateEnvironmentalHealth(greenScore, carbonFootprint);
        BigDecimal financialHealth = financialHealthService.calculate(companyId, LocalDate.now());
        String riskLevel = calculateRiskLevel(financialHealth, environmentalHealth);
        
        GreenPassportDTO passport = new GreenPassportDTO(
                financialHealth,
                environmentalHealth,
                carbonFootprint,
                greenSuppliers,
                riskLevel);
                
        return ResponseEntity.ok(corfoBenefitSimulator.simulate(passport));
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
