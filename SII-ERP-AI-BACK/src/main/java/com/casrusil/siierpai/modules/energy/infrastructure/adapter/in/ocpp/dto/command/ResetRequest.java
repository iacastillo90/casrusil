package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: Reset Request (Server → Charger).
 * Comando para reiniciar un cargador.
 */
public record ResetRequest(
        String type // "Soft" | "Hard"
) {
}
