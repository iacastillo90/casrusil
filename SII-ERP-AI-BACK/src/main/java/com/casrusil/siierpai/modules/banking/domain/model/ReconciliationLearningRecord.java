package com.casrusil.siierpai.modules.banking.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.Instant;
import java.util.UUID;

/**
 * 🧠 FERRARI ENGINE PHASE 3: Registro de aprendizaje de conciliación.
 * 
 * <p>
 * Almacena las decisiones del usuario (confirmar/rechazar matches)
 * para mejorar el algoritmo de matching en el futuro.
 * 
 * <h2>Feedback Loop:</h2>
 * <ol>
 * <li>Sistema sugiere match con confidence X</li>
 * <li>Usuario confirma o rechaza</li>
 * <li>Se guarda la decisión en LearningRecord</li>
 * <li>Próxima vez, el algoritmo ajusta pesos basándose en historial</li>
 * </ol>
 * 
 * @since 2.0 - Ferrari Engine Phase 3
 */
public class ReconciliationLearningRecord {

    private final UUID id;
    private final CompanyId companyId;
    private final UUID bankTransactionId;
    private final UUID accountingEntryId;
    private final double suggestedConfidence;
    private final boolean userConfirmed;
    private final String userFeedback; // Opcional: por qué rechazó
    private final Instant createdAt;

    private ReconciliationLearningRecord(
            UUID id,
            CompanyId companyId,
            UUID bankTransactionId,
            UUID accountingEntryId,
            double suggestedConfidence,
            boolean userConfirmed,
            String userFeedback,
            Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.bankTransactionId = bankTransactionId;
        this.accountingEntryId = accountingEntryId;
        this.suggestedConfidence = suggestedConfidence;
        this.userConfirmed = userConfirmed;
        this.userFeedback = userFeedback;
        this.createdAt = createdAt;
    }

    public static ReconciliationLearningRecord create(
            CompanyId companyId,
            UUID bankTransactionId,
            UUID accountingEntryId,
            double suggestedConfidence,
            boolean userConfirmed,
            String userFeedback) {
        return new ReconciliationLearningRecord(
                UUID.randomUUID(),
                companyId,
                bankTransactionId,
                accountingEntryId,
                suggestedConfidence,
                userConfirmed,
                userFeedback,
                Instant.now());
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public UUID getBankTransactionId() {
        return bankTransactionId;
    }

    public UUID getAccountingEntryId() {
        return accountingEntryId;
    }

    public double getSuggestedConfidence() {
        return suggestedConfidence;
    }

    public boolean isUserConfirmed() {
        return userConfirmed;
    }

    public String getUserFeedback() {
        return userFeedback;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
