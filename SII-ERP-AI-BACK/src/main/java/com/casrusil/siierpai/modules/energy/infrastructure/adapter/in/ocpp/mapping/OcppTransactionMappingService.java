package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.mapping;

import com.casrusil.siierpai.modules.energy.domain.model.SessionId;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de mapeo entre Transaction ID (OCPP) y Session ID (Dominio).
 * 
 * OCPP usa integers para transactionId, pero nuestro dominio usa UUIDs.
 * Este servicio mantiene el mapeo bidireccional.
 */
@Service
public class OcppTransactionMappingService {

    // Generador de transaction IDs
    private final AtomicInteger transactionIdGenerator = new AtomicInteger(1);

    // Mapeo bidireccional
    private final Map<Integer, SessionId> transactionToSession = new ConcurrentHashMap<>();
    private final Map<SessionId, Integer> sessionToTransaction = new ConcurrentHashMap<>();

    /**
     * Crea un nuevo transaction ID para una sesión.
     */
    public int createTransactionId(SessionId sessionId) {
        int transactionId = transactionIdGenerator.getAndIncrement();

        transactionToSession.put(transactionId, sessionId);
        sessionToTransaction.put(sessionId, transactionId);

        return transactionId;
    }

    /**
     * Encuentra el SessionId correspondiente a un transaction ID.
     */
    public Optional<SessionId> findSessionIdByTransactionId(int transactionId) {
        return Optional.ofNullable(transactionToSession.get(transactionId));
    }

    /**
     * Encuentra el transaction ID correspondiente a un SessionId.
     */
    public Optional<Integer> findTransactionIdBySessionId(SessionId sessionId) {
        return Optional.ofNullable(sessionToTransaction.get(sessionId));
    }

    /**
     * Elimina un mapeo cuando la transacción termina.
     */
    public void removeMapping(int transactionId) {
        SessionId sessionId = transactionToSession.remove(transactionId);
        if (sessionId != null) {
            sessionToTransaction.remove(sessionId);
        }
    }

    /**
     * Limpia todos los mapeos.
     */
    public void clearAll() {
        transactionToSession.clear();
        sessionToTransaction.clear();
    }
}
