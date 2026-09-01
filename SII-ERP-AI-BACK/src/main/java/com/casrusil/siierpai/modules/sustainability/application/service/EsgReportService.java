package com.casrusil.siierpai.modules.sustainability.application.service;

import com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics;
import com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics.ImpactCategory;
import com.casrusil.siierpai.modules.sustainability.domain.port.out.GreenMetricsRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera el reporte ESG (Environmental, Social, Governance) a partir de las
 * metricas verdes reales registradas por la empresa, agregando la huella de
 * carbono por alcance (Scope 1, 2 y 3) segun el GHG Protocol.
 *
 * <p>Reemplaza la version anterior que devolvia datos mock. Las recomendaciones
 * se derivan del scope con mayor impacto real, no de un texto fijo.
 */
@Service
public class EsgReportService {

    private final GreenMetricsRepository greenMetricsRepository;

    public EsgReportService(GreenMetricsRepository greenMetricsRepository) {
        this.greenMetricsRepository = greenMetricsRepository;
    }

    public EsgReport generateReport(CompanyId companyId) {
        List<GreenMetrics> metrics = greenMetricsRepository.findByCompanyId(companyId);

        BigDecimal scope1 = sumByCategory(metrics, ImpactCategory.SCOPE_1_EMISSION);
        BigDecimal scope2 = sumByCategory(metrics, ImpactCategory.SCOPE_2_EMISSION);
        BigDecimal scope3 = sumByCategory(metrics, ImpactCategory.SCOPE_3_EMISSION);
        BigDecimal total = scope1.add(scope2).add(scope3);

        return new EsgReport(scope1, scope2, scope3, total,
                buildRecommendations(scope1, scope2, scope3));
    }

    private BigDecimal sumByCategory(List<GreenMetrics> metrics, ImpactCategory category) {
        return metrics.stream()
                .filter(m -> m.getCategory() == category)
                .map(GreenMetrics::getCarbonFootprint)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Recomendaciones derivadas del alcance con mayor huella real. */
    private List<String> buildRecommendations(BigDecimal scope1, BigDecimal scope2, BigDecimal scope3) {
        List<String> recs = new ArrayList<>();
        BigDecimal max = scope1.max(scope2).max(scope3);

        if (max.signum() == 0) {
            recs.add("Aun no hay metricas ambientales registradas. Clasifique sus transacciones para obtener un reporte ESG.");
            return recs;
        }
        if (max.equals(scope3)) {
            recs.add("El Scope 3 (cadena de suministro) concentra su mayor huella: priorice proveedores con flota electrica o certificacion verde.");
        }
        if (max.equals(scope2)) {
            recs.add("El Scope 2 (electricidad) domina su huella: evalue autogeneracion solar o contratos de energia renovable.");
        }
        if (max.equals(scope1)) {
            recs.add("El Scope 1 (emisiones directas) es el mas alto: revise consumo de combustibles y climatizacion.");
        }
        recs.add("Compense emisiones mediante bonos de carbono para mejorar su score verde y el acceso a creditos CORFO.");
        return recs;
    }

    public record EsgReport(
        BigDecimal scope1Emissions,
        BigDecimal scope2Emissions,
        BigDecimal scope3Emissions,
        BigDecimal totalEmissions,
        List<String> aiRecommendations
    ) {}
}
