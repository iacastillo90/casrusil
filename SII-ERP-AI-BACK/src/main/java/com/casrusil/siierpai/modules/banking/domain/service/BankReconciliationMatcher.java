package com.casrusil.siierpai.modules.banking.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.banking.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.banking.domain.model.ReconciliationSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 🤝 FERRARI ENGINE PHASE 3: Motor de conciliación bancaria inteligente.
 * 
 * <p>
 * Encuentra automáticamente coincidencias entre transacciones bancarias
 * y asientos contables utilizando un algoritmo de scoring multi-criterio.
 * 
 * <h2>Algoritmo de Matching:</h2>
 * <ol>
 * <li>Monto exacto (±$100 tolerancia): +50%</li>
 * <li>Fecha cercana (±3 días): +30%</li>
 * <li>RUT en descripción: +20%</li>
 * <li>Nombre en descripción: +15%</li>
 * <li>Folio en descripción: +10%</li>
 * </ol>
 * 
 * <h2>Niveles de Confianza:</h2>
 * <ul>
 * <li><strong>Alta (≥80%)</strong>: Auto-match sugerido</li>
 * <li><strong>Media (50-80%)</strong>: Requiere revisión</li>
 * <li><strong>Baja (<50%)</strong>: Descartado</li>
 * </ul>
 * 
 * @see ReconciliationSuggestion
 * @since 2.0 - Ferrari Engine Phase 3
 */
@Service
public class BankReconciliationMatcher {

    private static final Logger logger = LoggerFactory.getLogger(BankReconciliationMatcher.class);

    // Tolerancias
    private static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("100"); // ±$100 CLP
    private static final long DATE_TOLERANCE_DAYS = 3;

    // Pesos del algoritmo
    private static final double WEIGHT_EXACT_AMOUNT = 0.50;
    private static final double WEIGHT_CLOSE_DATE = 0.30;
    private static final double WEIGHT_RUT_MATCH = 0.20;
    private static final double WEIGHT_NAME_MATCH = 0.15;
    private static final double WEIGHT_FOLIO_MATCH = 0.10;

    /**
     * Encuentra las mejores coincidencias para una transacción bancaria.
     * 
     * @param transaction Transacción bancaria a conciliar
     * @param candidates  Lista de asientos contables candidatos
     * @return Lista de sugerencias ordenadas por confianza (descendente)
     */
    public List<ReconciliationSuggestion> findMatches(
            BankTransaction transaction,
            List<AccountingEntry> candidates) {

        logger.debug("🤝 [RECONCILIATION] Buscando matches para: {} - ${}",
                transaction.getDescription(), transaction.getAmount());

        List<ReconciliationSuggestion> suggestions = new ArrayList<>();

        for (AccountingEntry entry : candidates) {
            double confidence = calculateConfidence(transaction, entry);

            if (confidence >= 0.5) { // Solo sugerencias con confianza mínima
                String reasons = buildMatchReasons(transaction, entry, confidence);
                suggestions.add(new ReconciliationSuggestion(
                        transaction.getId(),
                        entry,
                        confidence,
                        reasons));
            }
        }

        // Ordenar por confianza descendente
        suggestions.sort(Comparator.comparing(ReconciliationSuggestion::confidence).reversed());

        // Limitar a top 5 sugerencias
        if (suggestions.size() > 5) {
            suggestions = suggestions.subList(0, 5);
        }

        logger.info("✅ [RECONCILIATION] Encontradas {} sugerencias (top confidence: {}%)",
                suggestions.size(),
                suggestions.isEmpty() ? 0 : (int) (suggestions.get(0).confidence() * 100));

        return suggestions;
    }

    /**
     * Calcula el score de confianza entre una transacción y un asiento.
     */
    private double calculateConfidence(BankTransaction transaction, AccountingEntry entry) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();

        // 1. Monto exacto (±tolerancia)
        BigDecimal txAmount = transaction.getAmount().abs();

        // Calcular monto total del asiento (suma de débitos o créditos)
        BigDecimal entryAmount = entry.getLines().stream()
                .map(line -> line.debit().max(line.credit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal diff = txAmount.subtract(entryAmount).abs();

        if (diff.compareTo(AMOUNT_TOLERANCE) <= 0) {
            score += WEIGHT_EXACT_AMOUNT;
            reasons.add("Monto exacto");
        }

        // 2. Fecha cercana (±3 días)
        long daysDiff = Math.abs(ChronoUnit.DAYS.between(
                transaction.getDate(),
                entry.getEntryDate()));

        if (daysDiff <= DATE_TOLERANCE_DAYS) {
            double dateScore = WEIGHT_CLOSE_DATE * (1.0 - (daysDiff / (double) DATE_TOLERANCE_DAYS));
            score += dateScore;
            reasons.add("Fecha cercana (" + daysDiff + " días)");
        }

        // 3. RUT en descripción bancaria
        String txDesc = transaction.getDescription().toUpperCase();
        String partnerRut = entry.getTaxPayerId(); // Usar getTaxPayerId en lugar de getPartnerRut

        if (partnerRut != null && !partnerRut.isEmpty()) {
            // Normalizar RUT (quitar puntos y guión)
            String normalizedRut = partnerRut.replace(".", "").replace("-", "");
            if (txDesc.contains(normalizedRut) || txDesc.contains(partnerRut)) {
                score += WEIGHT_RUT_MATCH;
                reasons.add("RUT coincide");
            }
        }

        // 4. Nombre en descripción
        String partnerName = entry.getTaxPayerName(); // Usar getTaxPayerName en lugar de getPartnerName
        if (partnerName != null && !partnerName.isEmpty()) {
            String[] nameWords = partnerName.toUpperCase().split(" ");
            int matchedWords = 0;
            for (String word : nameWords) {
                if (word.length() > 3 && txDesc.contains(word)) {
                    matchedWords++;
                }
            }
            if (matchedWords > 0) {
                double nameScore = WEIGHT_NAME_MATCH * (matchedWords / (double) nameWords.length);
                score += nameScore;
                reasons.add("Nombre coincide");
            }
        }

        // 5. Folio/Número de documento
        String docNumber = entry.getDocumentNumber();
        if (docNumber != null && !docNumber.isEmpty() && txDesc.contains(docNumber)) {
            score += WEIGHT_FOLIO_MATCH;
            reasons.add("Folio coincide");
        }

        return Math.min(score, 1.0); // Cap at 1.0
    }

    /**
     * Construye una explicación legible de por qué se sugirió el match.
     */
    private String buildMatchReasons(BankTransaction transaction, AccountingEntry entry, double confidence) {
        StringBuilder sb = new StringBuilder();

        // Calcular monto total del asiento
        BigDecimal entryAmount = entry.getLines().stream()
                .map(line -> line.debit().max(line.credit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monto
        sb.append("Monto: $").append(transaction.getAmount().abs())
                .append(" vs $").append(entryAmount);

        // Fecha
        long daysDiff = Math.abs(ChronoUnit.DAYS.between(
                transaction.getDate(),
                entry.getEntryDate()));
        sb.append(" | Fecha: ").append(daysDiff).append(" días diff");

        // Partner
        if (entry.getTaxPayerName() != null) {
            sb.append(" | Partner: ").append(entry.getTaxPayerName());
        }

        // Confidence
        sb.append(" | Confidence: ").append((int) (confidence * 100)).append("%");

        return sb.toString();
    }
}
