package com.casrusil.siierpai.modules.accounting.infrastructure.web.dto;

/**
 * 🤝 FERRARI ENGINE PHASE 4: Estadísticas de conciliación bancaria.
 * 
 * <p>
 * Mover de banking module para evitar conflicto de entidades con bankingsync.
 * 
 * @param totalTransactions  Total de transacciones bancarias
 * @param reconciledCount    Transacciones conciliadas
 * @param pendingCount       Transacciones pendientes
 * @param reconciliationRate Tasa de conciliación (0.0 - 1.0)
 * @param averageConfidence  Confidence promedio de matches
 * @since 2.0 - Ferrari Engine Refactoring
 */
public record ReconciliationStatsDTO(
        int totalTransactions,
        int reconciledCount,
        int pendingCount,
        double reconciliationRate,
        double averageConfidence) {
}