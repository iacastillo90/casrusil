package com.casrusil.siierpai.modules.inventory.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Tipo de movimiento de inventario.
 *
 * <p>El contrato previo exponía {@code IN/OUT/ADJUSTMENT}, pero el front envía
 * {@code ENTRADA/SALIDA} (el vocabulario que usa el usuario). Para no romper a
 * ninguno de los dos se aceptan ambos conjuntos de valores.</p>
 */
public enum MovementType {
    IN, OUT, ADJUSTMENT;

    @JsonCreator
    public static MovementType fromJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toUpperCase()) {
            case "IN", "ENTRADA", "INGRESO" -> IN;
            case "OUT", "SALIDA", "EGRESO" -> OUT;
            case "ADJUSTMENT", "AJUSTE" -> ADJUSTMENT;
            default -> throw new IllegalArgumentException(
                    "Tipo de movimiento inválido: " + value + " (use ENTRADA, SALIDA o AJUSTE)");
        };
    }
}
