package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.validation;

import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.IdTagInfo;
import com.casrusil.siierpai.modules.fleet.domain.model.Vehicle;
import com.casrusil.siierpai.modules.fleet.domain.port.out.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de autorización RFID para OCPP.
 * Valida idTags contra la base de datos de vehículos/usuarios.
 * 
 * Flujo de autorización:
 * 1. Buscar vehículo por RFID tag
 * 2. Validar estado del vehículo
 * 3. Validar permisos de carga
 * 4. Retornar IdTagInfo con status
 */
@Service
public class RfidAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(RfidAuthorizationService.class);

    private final VehicleRepository vehicleRepository;

    @Value("${ocpp.authorization.dev-tags:}")
    private String devTagsConfig;

    @Value("${ocpp.authorization.dev-mode:false}")
    private boolean devMode;

    public RfidAuthorizationService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Autoriza un idTag (RFID).
     * 
     * @param idTag RFID tag del usuario/vehículo
     * @return IdTagInfo con status de autorización
     */
    public IdTagInfo authorize(String idTag) {
        logger.debug("Autorizando idTag: {}", idTag);

        // Validación básica
        if (idTag == null || idTag.isBlank()) {
            logger.warn("IdTag nulo o vacío");
            return new IdTagInfo("Invalid", null, null);
        }

        // En modo desarrollo, aceptar tags de prueba
        if (devMode && isDevTag(idTag)) {
            logger.info("Dev mode: Aceptando tag de desarrollo: {}", idTag);
            return new IdTagInfo("Accepted", LocalDateTime.now().plusYears(1).toString(), null);
        }

        // Buscar vehículo por RFID tag
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByRfidTag(idTag);

        if (vehicleOpt.isEmpty()) {
            logger.warn("IdTag no encontrado en base de datos: {}", idTag);
            return new IdTagInfo("Invalid", null, null);
        }

        Vehicle vehicle = vehicleOpt.get();

        // Validar estado del vehículo
        switch (vehicle.getStatus()) {
            case OUT_OF_SERVICE:
                logger.warn("Vehículo fuera de servicio: {}", vehicle.getLicensePlate());
                return new IdTagInfo("Blocked", null, null);

            case CHARGING:
                logger.warn("Vehículo ya está cargando: {}", vehicle.getLicensePlate());
                return new IdTagInfo("ConcurrentTx", null, null);

            case AVAILABLE:
            case IN_USE:
            case RESERVED:
                // Autorizado
                logger.info("IdTag autorizado para vehículo: {}", vehicle.getLicensePlate());
                return new IdTagInfo("Accepted", LocalDateTime.now().plusYears(1).toString(), null);

            default:
                logger.error("Estado de vehículo desconocido: {}", vehicle.getStatus());
                return new IdTagInfo("Invalid", null, null);
        }
    }

    /**
     * Verifica si un tag es un tag de desarrollo.
     */
    private boolean isDevTag(String idTag) {
        if (devTagsConfig == null || devTagsConfig.isBlank()) {
            return false;
        }

        List<String> devTags = Arrays.asList(devTagsConfig.split(","));
        return devTags.stream()
                .map(String::trim)
                .anyMatch(tag -> tag.equals(idTag));
    }
}
