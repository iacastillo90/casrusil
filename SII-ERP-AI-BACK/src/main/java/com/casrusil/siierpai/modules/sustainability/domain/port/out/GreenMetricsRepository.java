package com.casrusil.siierpai.modules.sustainability.domain.port.out;

import com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 🌿 Repositorio para persistencia de métricas ambientales.
 * 
 * @since 2.0 - Ferrari Engine Phase 2
 */
public interface GreenMetricsRepository {

    /**
     * Persiste una métrica ambiental.
     * 
     * @param metrics Métrica a persistir
     */
    void save(GreenMetrics metrics);

    /**
     * Encuentra todas las métricas de una empresa.
     * 
     * @param companyId ID de la empresa
     * @return Lista de métricas ambientales
     */
    List<GreenMetrics> findByCompanyId(CompanyId companyId);

    /**
     * Calcula la huella de carbono total de una empresa.
     * 
     * @param companyId ID de la empresa
     * @return Total de kg CO2e
     */
    BigDecimal calculateTotalCarbonFootprint(CompanyId companyId);

    /**
     * Calcula el score verde total de una empresa.
     * 
     * @param companyId ID de la empresa
     * @return Score verde acumulado
     */
    BigDecimal calculateTotalGreenScore(CompanyId companyId);

    /**
     * Cuenta proveedores con certificación verde.
     * 
     * @param companyId ID de la empresa
     * @return Número de proveedores verdes
     */
    long countGreenSuppliers(CompanyId companyId);
}
