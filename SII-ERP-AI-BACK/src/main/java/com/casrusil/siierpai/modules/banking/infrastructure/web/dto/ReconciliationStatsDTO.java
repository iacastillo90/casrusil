package com.casrusil.siierpai.modules.banking.infrastructure.web.dto;

/**
 * 🤝 FERRARI ENGINE PHASE 4: Estadísticas de conciliación bancaria.
 * 
 * @param totalTransactions  Total de transacciones bancarias
 * @param reconciledCount    Transacciones conciliadas
 * @param pendingCount       Transacciones pendientes
 * @param reconciliationRate Tasa de conciliación (0.0 - 1.0)
 * @param averageConfidence  Confidence promedio de matches
 * @since 2.0 - Ferrari Engine Phase 4
 */
public record ReconciliationStatsDTO(
        int totalTransactions,
        int reconciledCount,
        int pendingCount,
        double reconciliationRate,
        double averageConfidence) {
}
