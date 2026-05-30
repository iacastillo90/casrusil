package com.casrusil.siierpai.modules.sustainability.domain.service;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics;
import com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics.ImpactCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 🌿 FERRARI ENGINE: Clasificador dual de transacciones (Contable + Ambiental).
 * 
 * <p>
 * Este servicio analiza cada factura y determina su impacto ambiental
 * basándose en palabras clave en la glosa y el tipo de proveedor.
 * 
 * <h2>Reglas de Clasificación:</h2>
 * <ul>
 * <li><strong>Combustibles</strong>: SCOPE_1_EMISSION, ~2.5 kg CO2e por $1000
 * CLP</li>
 * <li><strong>Electricidad</strong>: SCOPE_2_EMISSION, ~0.4 kg CO2e por
 * kWh</li>
 * <li><strong>Transporte</strong>: SCOPE_3_EMISSION, variable según
 * distancia</li>
 * <li><strong>Energía Renovable</strong>: RENEWABLE_ENERGY, score +50</li>
 * <li><strong>Reciclaje</strong>: WASTE_MANAGEMENT, score +20</li>
 * </ul>
 * 
 * @see GreenMetrics
 * @since 2.0 - Ferrari Engine Phase 2
 */
@Service
public class SustainabilityClassifier {

    private static final Logger logger = LoggerFactory.getLogger(SustainabilityClassifier.class);

    // Factores de emisión (kg CO2e)
    private static final BigDecimal FUEL_EMISSION_FACTOR = new BigDecimal("0.0025"); // por $1 CLP
    private static final BigDecimal ELECTRICITY_EMISSION_FACTOR = new BigDecimal("0.4"); // por kWh
    private static final BigDecimal TRANSPORT_EMISSION_FACTOR = new BigDecimal("0.15"); // por km

    /**
     * Clasifica una factura y genera métricas ambientales si aplica.
     * 
     * @param invoice     Factura a clasificar
     * @param accountCode Código de cuenta contable asignado
     * @return Métricas ambientales, o empty si no aplica clasificación
     */
    public Optional<GreenMetrics> classify(Invoice invoice, String accountCode) {
        String glosa = buildGlosa(invoice);
        String glosaUpper = glosa.toUpperCase();

        logger.debug("🌿 [SUSTAINABILITY] Clasificando: {}", glosa);

        // 1. SCOPE 1: Combustibles (Emisiones directas)
        if (containsAny(glosaUpper, "COMBUSTIBLE", "GASOLINA", "DIESEL", "GAS", "PETRÓLEO", "GLP")) {
            BigDecimal emissions = invoice.getNetAmount().multiply(FUEL_EMISSION_FACTOR);
            logger.info("🔥 [SCOPE 1] Combustible detectado: {} kg CO2e", emissions);

            return Optional.of(GreenMetrics.create(
                    invoice.getCompanyId(),
                    null, // Se asignará el accountingEntryId en el listener
                    ImpactCategory.SCOPE_1_EMISSION,
                    emissions,
                    new BigDecimal("-10"), // Penalización
                    "Compra de combustibles fósiles: " + glosa));
        }

        // 2. SCOPE 2: Electricidad (Emisiones indirectas)
        if (containsAny(glosaUpper, "ELECTRICIDAD", "ENERGIA ELECTRICA", "LUZ", "ENEL", "CGE")) {
            // Estimación: $50,000 CLP ≈ 100 kWh
            BigDecimal estimatedKwh = invoice.getNetAmount().divide(new BigDecimal("500"), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal emissions = estimatedKwh.multiply(ELECTRICITY_EMISSION_FACTOR);

            logger.info("⚡ [SCOPE 2] Electricidad detectada: {} kWh ≈ {} kg CO2e", estimatedKwh, emissions);

            return Optional.of(GreenMetrics.create(
                    invoice.getCompanyId(),
                    null,
                    ImpactCategory.SCOPE_2_EMISSION,
                    emissions,
                    new BigDecimal("-5"), // Penalización menor
                    "Consumo de electricidad: " + glosa));
        }

        // 3. SCOPE 3: Transporte y Logística
        if (containsAny(glosaUpper, "TRANSPORTE", "FLETE", "LOGISTICA", "ENVIO", "COURIER")) {
            BigDecimal emissions = invoice.getNetAmount().multiply(new BigDecimal("0.001"));

            logger.info("🚚 [SCOPE 3] Transporte detectado: {} kg CO2e", emissions);

            return Optional.of(GreenMetrics.create(
                    invoice.getCompanyId(),
                    null,
                    ImpactCategory.SCOPE_3_EMISSION,
                    emissions,
                    new BigDecimal("-3"),
                    "Servicios de transporte: " + glosa));
        }

        // 4. RENEWABLE ENERGY: Inversión en energías renovables (POSITIVO)
        if (containsAny(glosaUpper, "PANEL SOLAR", "ENERGIA RENOVABLE", "FOTOVOLTAICO", "EOLICA", "SOLAR")) {
            logger.info("☀️ [RENEWABLE] Energía renovable detectada: +50 score");

            return Optional.of(GreenMetrics.create(
                    invoice.getCompanyId(),
                    null,
                    ImpactCategory.RENEWABLE_ENERGY,
                    BigDecimal.ZERO, // Sin emisiones
                    new BigDecimal("50"), // Bonificación grande
                    "Inversión en energía renovable: " + glosa));
        }

        // 5. WASTE MANAGEMENT: Reciclaje y gestión de residuos (POSITIVO)
        if (containsAny(glosaUpper, "RECICLAJE", "RESIDUOS", "COMPOST", "REUTILIZACION")) {
            logger.info("♻️ [WASTE] Gestión de residuos detectada: +20 score");

            return Optional.of(GreenMetrics.create(
                    invoice.getCompanyId(),
                    null,
                    ImpactCategory.WASTE_MANAGEMENT,
                    BigDecimal.ZERO,
                    new BigDecimal("20"),
                    "Gestión sostenible de residuos: " + glosa));
        }

        // 6. CARBON OFFSET: Bonos de carbono (POSITIVO)
        if (containsAny(glosaUpper, "BONO CARBONO", "COMPENSACION", "OFFSET", "CREDITO CARBONO")) {
            logger.info("🌳 [OFFSET] Compensación de carbono detectada: +30 score");

            return Optional.of(GreenMetrics.create(
                    invoice.getCompanyId(),
                    null,
                    ImpactCategory.CARBON_OFFSET,
                    invoice.getNetAmount().multiply(new BigDecimal("-1")), // Compensación negativa
                    new BigDecimal("30"),
                    "Compensación de huella de carbono: " + glosa));
        }

        // No aplica clasificación ambiental
        logger.debug("⚪ [NEUTRAL] Sin impacto ambiental significativo: {}", glosa);
        return Optional.empty();
    }

    /**
     * Construye la glosa completa para análisis.
     */
    private String buildGlosa(Invoice invoice) {
        StringBuilder glosa = new StringBuilder();
        glosa.append(invoice.getBusinessName()).append(" ");
        if (invoice.getItemsSummary() != null) {
            glosa.append(invoice.getItemsSummary());
        }
        return glosa.toString();
    }

    /**
     * Verifica si el texto contiene alguna de las palabras clave.
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
