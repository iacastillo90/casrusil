package com.casrusil.siierpai.modules.banking.domain.model;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;

import java.util.UUID;

/**
 * 🤝 FERRARI ENGINE PHASE 3: Sugerencia de conciliación bancaria.
 * 
 * <p>
 * Representa una posible coincidencia entre una transacción bancaria
 * y un asiento contable, con un score de confianza calculado por IA.
 * 
 * <h2>Algoritmo de Confidence:</h2>
 * <ul>
 * <li><strong>Monto exacto</strong>: +50%</li>
 * <li><strong>Fecha cercana (±3 días)</strong>: +30%</li>
 * <li><strong>RUT en glosa</strong>: +20%</li>
 * <li><strong>Nombre en glosa</strong>: +15%</li>
 * <li><strong>Folio en glosa</strong>: +10%</li>
 * </ul>
 * 
 * <p>
 * Confidence > 0.8 = Auto-match sugerido
 * <p>
 * Confidence 0.5-0.8 = Requiere revisión
 * <p>
 * Confidence < 0.5 = No mostrar
 * 
 * @param bankTransactionId ID de la transacción bancaria
 * @param accountingEntry   Asiento contable candidato
 * @param confidence        Score de confianza (0.0 - 1.0)
 * @param matchReasons      Razones del match (para explicabilidad)
 * @since 2.0 - Ferrari Engine Phase 3
 */
public record ReconciliationSuggestion(
        UUID bankTransactionId,
        AccountingEntry accountingEntry,
        double confidence,
        String matchReasons) {

    /**
     * Verifica si la sugerencia es de alta confianza (auto-match).
     */
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    /**
     * Verifica si la sugerencia requiere revisión manual.
     */
    public boolean requiresReview() {
        return confidence >= 0.5 && confidence < 0.8;
    }

    /**
     * Verifica si la sugerencia debe descartarse.
     */
    public boolean shouldDiscard() {
        return confidence < 0.5;
    }
}
