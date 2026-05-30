package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.mapping;

import com.casrusil.siierpai.modules.energy.domain.model.OcppIdentifier;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingStationRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de mapeo entre OCPP ID y Station ID.
 * 
 * Mantiene un caché en memoria para mapeo rápido y consulta
 * la base de datos cuando es necesario.
 */
@Service
public class OcppStationMappingService {

    private final ChargingStationRepository stationRepository;

    // Caché en memoria: ocppId → stationId
    private final Map<String, StationId> ocppToStationCache = new ConcurrentHashMap<>();

    public OcppStationMappingService(ChargingStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    /**
     * Encuentra el StationId correspondiente a un OCPP ID.
     * Primero busca en caché, luego en BD.
     */
    public Optional<StationId> findStationIdByOcppId(OcppIdentifier ocppId) {
        String ocppIdValue = ocppId.getValue();

        // 1. Buscar en caché
        StationId cached = ocppToStationCache.get(ocppIdValue);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Si no está en caché, buscar en BD (TODO: implementar cuando exista el
        // repository)
        // Por ahora, generar un nuevo StationId
        StationId stationId = StationId.generate();
        ocppToStationCache.put(ocppId.getValue(), stationId);
        return Optional.of(stationId);
    }

    /**
     * Registra un nuevo mapeo en el caché.
     * Se llama cuando se registra una nueva estación.
     */
    public void registerMapping(OcppIdentifier ocppId, StationId stationId) {
        ocppToStationCache.put(ocppId.getValue(), stationId);
    }

    /**
     * Limpia el caché de mapeo.
     */
    public void clearCache() {
        ocppToStationCache.clear();
    }
}
