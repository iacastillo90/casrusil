package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.parser;

import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.model.OcppMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Parser de mensajes OCPP 1.6 JSON.
 * 
 * Formato OCPP:
 * - CALL: [2, "uniqueId", "action", {...payload...}]
 * - CALLRESULT: [3, "uniqueId", {...payload...}]
 * - CALLERROR: [4, "uniqueId", "errorCode", "errorDescription",
 * {...details...}]
 */
@Component
public class OcppMessageParser {

    private final ObjectMapper objectMapper;

    public OcppMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parsea un mensaje OCPP JSON.
     */
    public OcppMessage parseMessage(String json) throws Exception {
        JsonNode array = objectMapper.readTree(json);

        if (!array.isArray() || array.size() < 3) {
            throw new IllegalArgumentException("Mensaje OCPP inválido: " + json);
        }

        int messageTypeId = array.get(0).asInt();
        String uniqueId = array.get(1).asText();

        return switch (messageTypeId) {
            case 2 -> parseCall(array, uniqueId);
            case 3 -> parseCallResult(array, uniqueId);
            case 4 -> parseCallError(array, uniqueId);
            default -> throw new IllegalArgumentException("MessageTypeId no soportado: " + messageTypeId);
        };
    }

    private OcppMessage parseCall(JsonNode array, String uniqueId) {
        String action = array.get(2).asText();
        JsonNode payload = array.get(3);
        return new OcppMessage(2, uniqueId, action, payload, null, null);
    }

    private OcppMessage parseCallResult(JsonNode array, String uniqueId) {
        JsonNode payload = array.get(2);
        return new OcppMessage(3, uniqueId, null, payload, null, null);
    }

    private OcppMessage parseCallError(JsonNode array, String uniqueId) {
        String errorCode = array.get(2).asText();
        String errorDescription = array.get(3).asText();
        JsonNode details = array.size() > 4 ? array.get(4) : objectMapper.createObjectNode();
        return new OcppMessage(4, uniqueId, null, details, errorCode, errorDescription);
    }

    /**
     * Serializa un comando CALL (Server → Charger).
     */
    public String serializeCall(String uniqueId, String action, Object payload) throws Exception {
        JsonNode payloadNode = objectMapper.valueToTree(payload);
        JsonNode array = objectMapper.createArrayNode()
                .add(2)
                .add(uniqueId)
                .add(action)
                .add(payloadNode);
        return objectMapper.writeValueAsString(array);
    }

    /**
     * Serializa una respuesta CALLRESULT.
     */
    public String serializeCallResult(String uniqueId, Object payload) throws Exception {
        JsonNode payloadNode = objectMapper.valueToTree(payload);
        JsonNode array = objectMapper.createArrayNode()
                .add(3)
                .add(uniqueId)
                .add(payloadNode);
        return objectMapper.writeValueAsString(array);
    }

    /**
     * Serializa un error CALLERROR.
     */
    public String serializeCallError(String uniqueId, String errorCode, String errorDescription) throws Exception {
        JsonNode array = objectMapper.createArrayNode()
                .add(4)
                .add(uniqueId)
                .add(errorCode)
                .add(errorDescription)
                .add(objectMapper.createObjectNode());
        return objectMapper.writeValueAsString(array);
    }
}
