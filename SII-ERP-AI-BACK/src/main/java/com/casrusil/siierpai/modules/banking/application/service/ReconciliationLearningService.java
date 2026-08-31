package com.casrusil.siierpai.modules.banking.application.service;

import com.casrusil.siierpai.modules.banking.domain.model.ReconciliationLearningRecord;
import com.casrusil.siierpai.modules.banking.infrastructure.persistence.ReconciliationLearningEntity;
import com.casrusil.siierpai.modules.banking.infrastructure.persistence.ReconciliationLearningJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/**
 * 🧠 FERRARI ENGINE PHASE 3: Servicio de aprendizaje de conciliación.
 * 
 * <p>
 * Implementa un feedback loop que mejora el algoritmo de matching
 * basándose en las decisiones del usuario.
 * 
 * <h2>Cómo Funciona:</h2>
 * <ol>
 * <li>Usuario confirma o rechaza un match sugerido</li>
 * <li>Se guarda la decisión con el confidence original</li>
 * <li>El sistema analiza patrones:
 * <ul>
 * <li>Si confidence 90% fue rechazado → ajustar pesos</li>
 * <li>Si confidence 60% fue confirmado → aumentar tolerancia</li>
 * </ul>
 * </li>
 * <li>Próximas sugerencias son más precisas</li>
 * </ol>
 * 
 * <h2>Métricas de Aprendizaje:</h2>
 * <ul>
 * <li><strong>Precision</strong>: % de sugerencias confirmadas</li>
 * <li><strong>False Positives</strong>: Alta confidence pero rechazado</li>
 * <li><strong>False Negatives</strong>: Baja confidence pero confirmado</li>
 * </ul>
 * 
 * @since 2.0 - Ferrari Engine Phase 3
 */
@Service
public class ReconciliationLearningService {

    private static final Logger logger = LoggerFactory.getLogger(ReconciliationLearningService.class);

    private final ReconciliationLearningJpaRepository repository;

    public ReconciliationLearningService(ReconciliationLearningJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra la decisión del usuario sobre un match sugerido.
     * 
     * @param companyId           ID de la empresa
     * @param bankTransactionId   ID de la transacción bancaria
     * @param accountingEntryId   ID del asiento contable
     * @param suggestedConfidence Confidence que el sistema sugirió
     * @param userConfirmed       Si el usuario confirmó el match
     * @param userFeedback        Feedback opcional del usuario
     */
    @Transactional
    public void recordDecision(
            CompanyId companyId,
            UUID bankTransactionId,
            UUID accountingEntryId,
            double suggestedConfidence,
            boolean userConfirmed,
            String userFeedback) {

        ReconciliationLearningRecord record = ReconciliationLearningRecord.create(
                companyId,
                bankTransactionId,
                accountingEntryId,
                suggestedConfidence,
                userConfirmed,
                userFeedback);

        // Persist to DB
        ReconciliationLearningEntity entity = new ReconciliationLearningEntity();
        entity.setId(record.getId());
        entity.setCompanyId(record.getCompanyId().value().toString());
        entity.setBankTransactionId(record.getBankTransactionId());
        entity.setAccountingEntryId(record.getAccountingEntryId());
        entity.setSuggestedConfidence(record.getSuggestedConfidence());
        entity.setUserConfirmed(record.isUserConfirmed());
        entity.setUserFeedback(record.getUserFeedback());
        entity.setCreatedAt(record.getCreatedAt());
        repository.save(entity);

        // Analizar si es un caso interesante para logging
        if (suggestedConfidence >= 0.8 && !userConfirmed) {
            logger.warn("🧠 [LEARNING] FALSE POSITIVE: Confidence {}% rechazado por usuario. Feedback: {}",
                    (int) (suggestedConfidence * 100), userFeedback);
        } else if (suggestedConfidence < 0.6 && userConfirmed) {
            logger.info("🧠 [LEARNING] FALSE NEGATIVE: Confidence {}% confirmado por usuario. Ajustar algoritmo.",
                    (int) (suggestedConfidence * 100));
        } else if (userConfirmed) {
            logger.info("✅ [LEARNING] TRUE POSITIVE: Confidence {}% confirmado correctamente.",
                    (int) (suggestedConfidence * 100));
        }
    }

    /**
     * Calcula la precisión del algoritmo de matching.
     * 
     * @param companyId ID de la empresa
     * @return Precision (0.0 - 1.0)
     */
    public double calculatePrecision(CompanyId companyId) {
        var records = repository.findByCompanyId(companyId.value().toString());
        if (records.isEmpty()) return 0.0;
        long confirmed = records.stream().filter(ReconciliationLearningEntity::isUserConfirmed).count();
        return (double) confirmed / records.size();
    }

    /**
     * Obtiene estadísticas de aprendizaje para una empresa.
     * 
     * @param companyId ID de la empresa
     * @return Estadísticas en formato legible
     */
    public String getStats(CompanyId companyId) {
        var companyRecords = repository.findByCompanyId(companyId.value().toString());

        if (companyRecords.isEmpty()) {
            return "Sin datos de aprendizaje aún";
        }

        long total = companyRecords.size();
        long confirmed = companyRecords.stream()
                .filter(r -> r.isUserConfirmed())
                .count();
        long rejected = total - confirmed;

        double precision = (double) confirmed / total;

        // Analizar false positives (alta confidence pero rechazado)
        long falsePositives = companyRecords.stream()
                .filter(r -> r.getSuggestedConfidence() >= 0.8 && !r.isUserConfirmed())
                .count();

        // Analizar false negatives (baja confidence pero confirmado)
        long falseNegatives = companyRecords.stream()
                .filter(r -> r.getSuggestedConfidence() < 0.6 && r.isUserConfirmed())
                .count();

        return String.format(
                "Total: %d | Confirmados: %d | Rechazados: %d | Precision: %.1f%% | FP: %d | FN: %d",
                total, confirmed, rejected, precision * 100, falsePositives, falseNegatives);
    }

    /**
     * Sugiere ajustes al algoritmo basándose en el historial.
     * 
     * @param companyId ID de la empresa
     * @return Sugerencias de ajuste
     */
    public List<String> suggestAlgorithmAdjustments(CompanyId companyId) {
        List<String> suggestions = new ArrayList<>();
        var companyRecords = repository.findByCompanyId(companyId.value().toString());

        if (companyRecords.size() < 10) {
            suggestions.add("Necesitas al menos 10 decisiones para sugerencias precisas");
            return suggestions;
        }

        // Analizar false positives
        long falsePositives = companyRecords.stream()
                .filter(r -> r.getSuggestedConfidence() >= 0.8 && !r.isUserConfirmed())
                .count();

        if (falsePositives > companyRecords.size() * 0.2) {
            suggestions.add("Muchos false positives: Considerar aumentar threshold de auto-match a 90%");
        }

        // Analizar false negatives
        long falseNegatives = companyRecords.stream()
                .filter(r -> r.getSuggestedConfidence() < 0.6 && r.isUserConfirmed())
                .count();

        if (falseNegatives > companyRecords.size() * 0.1) {
            suggestions.add("Muchos false negatives: Considerar aumentar peso de fecha o nombre");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Algoritmo funcionando bien, sin ajustes necesarios");
        }

        return suggestions;
    }
}
