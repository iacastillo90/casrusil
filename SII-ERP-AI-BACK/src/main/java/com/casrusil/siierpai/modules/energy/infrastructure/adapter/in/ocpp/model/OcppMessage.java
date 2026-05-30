package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Mensaje OCPP parseado.
 * 
 * Formato OCPP 1.6:
 * - CALL: [2, "uniqueId", "action", payload]
 * - CALLRESULT: [3, "uniqueId", payload]
 * - CALLERROR: [4, "uniqueId", "errorCode", "errorDescription", details]
 */
public record OcppMessage(
        int messageTypeId, // 2=CALL, 3=CALLRESULT, 4=CALLERROR
        String uniqueId,
        String action, // Solo para CALL
        JsonNode payload,
        String errorCode, // Solo para CALLERROR
        String errorDescription // Solo para CALLERROR
) {

    public boolean isCall() {
        return messageTypeId == 2;
    }

    public boolean isCallResult() {
        return messageTypeId == 3;
    }

    public boolean isCallError() {
        return messageTypeId == 4;
    }
}
