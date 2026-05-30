package com.casrusil.siierpai.modules.sustainability.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 🌿 GREEN PASSPORT: Métricas de impacto ambiental asociadas a transacciones
 * contables.
 * 
 * <p>
 * Este modelo de dominio representa la huella de carbono y el score ambiental
 * de cada transacción comercial, permitiendo a las empresas:
 * <ul>
 * <li>Calcular su huella de carbono total</li>
 * <li>Obtener un "Pasaporte Verde" para acceso a créditos CORFO</li>
 * <li>Identificar proveedores sostenibles</li>
 * <li>Generar reportes ESG (Environmental, Social, Governance)</li>
 * </ul>
 * 
 * <h2>Clasificación de Impacto:</h2>
 * <ul>
 * <li><strong>SCOPE_1_EMISSION</strong>: Emisiones directas (combustibles,
 * gas)</li>
 * <li><strong>SCOPE_2_EMISSION</strong>: Emisiones indirectas
 * (electricidad)</li>
 * <li><strong>SCOPE_3_EMISSION</strong>: Otras indirectas (transporte,
 * logística)</li>
 * <li><strong>RENEWABLE_ENERGY</strong>: Inversión en energías renovables
 * (+score)</li>
 * <li><strong>WASTE_MANAGEMENT</strong>: Gestión de residuos y reciclaje</li>
 * <li><strong>CARBON_OFFSET</strong>: Compensación de carbono (bonos)</li>
 * <li><strong>SUSTAINABLE_SUPPLY</strong>: Proveedores con certificación
 * verde</li>
 * </ul>
 * 
 * <h2>Ejemplo de Uso:</h2>
 * 
 * <pre>
 * // Factura de combustible
 * GreenMetrics fuelMetrics = GreenMetrics.create(
 *         companyId,
 *         accountingEntryId,
 *         ImpactCategory.SCOPE_1_EMISSION,
 *         new BigDecimal("125.5"), // kg CO2e
 *         new BigDecimal("-10"), // Penalización
 *         "Compra de gasolina 95 octanos");
 * 
 * // Inversión en paneles solares
 * GreenMetrics solarMetrics = GreenMetrics.create(
 *         companyId,
 *         accountingEntryId,
 *         ImpactCategory.RENEWABLE_ENERGY,
 *         BigDecimal.ZERO, // Sin emisiones
 *         new BigDecimal("50"), // Bonificación
 *         "Instalación de paneles solares 10kW");
 * </pre>
 * 
 * @see ImpactCategory
 * @see com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry
 * @since 2.0 - Ferrari Engine Phase 2
 */
public class GreenMetrics {

    private final UUID id;
    private final CompanyId companyId;
    private final UUID accountingEntryId;
    private final ImpactCategory category;
    private final BigDecimal carbonFootprint; // kg CO2e (equivalente de CO2)
    private final BigDecimal greenScore; // -100 a +100
    private final String evidence;
    private final Instant createdAt;

    /**
     * Constructor privado - usar factory method create()
     */
    private GreenMetrics(
            UUID id,
            CompanyId companyId,
            UUID accountingEntryId,
            ImpactCategory category,
            BigDecimal carbonFootprint,
            BigDecimal greenScore,
            String evidence,
            Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.accountingEntryId = accountingEntryId;
        this.category = category;
        this.carbonFootprint = carbonFootprint;
        this.greenScore = greenScore;
        this.evidence = evidence;
        this.createdAt = createdAt;
    }

    /**
     * Factory method para crear nuevas métricas verdes.
     * 
     * @param companyId         ID de la empresa
     * @param accountingEntryId ID del asiento contable relacionado
     * @param category          Categoría de impacto ambiental
     * @param carbonFootprint   Huella de carbono en kg CO2e
     * @param greenScore        Score verde (-100 a +100)
     * @param evidence          Justificación/evidencia de la clasificación
     * @return Nueva instancia de GreenMetrics
     */
    public static GreenMetrics create(
            CompanyId companyId,
            UUID accountingEntryId,
            ImpactCategory category,
            BigDecimal carbonFootprint,
            BigDecimal greenScore,
            String evidence) {
        return new GreenMetrics(
                UUID.randomUUID(),
                companyId,
                accountingEntryId,
                category,
                carbonFootprint,
                greenScore,
                evidence,
                Instant.now());
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public UUID getAccountingEntryId() {
        return accountingEntryId;
    }

    public ImpactCategory getCategory() {
        return category;
    }

    public BigDecimal getCarbonFootprint() {
        return carbonFootprint;
    }

    public BigDecimal getGreenScore() {
        return greenScore;
    }

    public String getEvidence() {
        return evidence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Categorías de impacto ambiental según GHG Protocol.
     */
    public enum ImpactCategory {
        /**
         * Scope 1: Emisiones directas de fuentes controladas por la empresa.
         * Ejemplos: Combustibles, gas natural, refrigerantes.
         */
        SCOPE_1_EMISSION,

        /**
         * Scope 2: Emisiones indirectas por consumo de energía comprada.
         * Ejemplos: Electricidad, vapor, calefacción.
         */
        SCOPE_2_EMISSION,

        /**
         * Scope 3: Otras emisiones indirectas en la cadena de valor.
         * Ejemplos: Transporte de mercancías, viajes de negocios.
         */
        SCOPE_3_EMISSION,

        /**
         * Inversión en energías renovables (score positivo).
         * Ejemplos: Paneles solares, turbinas eólicas.
         */
        RENEWABLE_ENERGY,

        /**
         * Gestión de residuos y reciclaje (score positivo).
         */
        WASTE_MANAGEMENT,

        /**
         * Compensación de carbono mediante bonos (score positivo).
         */
        CARBON_OFFSET,

        /**
         * Compras a proveedores con certificación verde (score positivo).
         */
        SUSTAINABLE_SUPPLY
    }
}
