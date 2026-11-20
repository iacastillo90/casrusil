package com.casrusil.siierpai.modules.integration_sii.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiDteQueryResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Traduce el ESTADO devuelto por el SII a los estados del dominio.
 *
 * <p>
 * El vocabulario de códigos no está en el WSDL, por lo que es configurable:
 * {@code sii.estado.aceptados}, {@code sii.estado.aceptados-reparos} y
 * {@code sii.estado.rechazados}. Los valores por defecto son los códigos
 * documentados por el SII (EPR/DOK aceptados; RCT/RFT/RSC/RCI/RCH/RFR/RTC/RCO/
 * FAU rechazados).
 * </p>
 *
 * <p>
 * Un código desconocido devuelve {@link SiiDocumentStatus#PENDIENTE}: no se
 * cambia el estado de la factura ni se genera asiento.
 * </p>
 */
@Component
public class SiiDteStatusMapper {

    public enum SiiDocumentStatus {
        ACEPTADO,
        ACEPTADO_CON_REPAROS,
        RECHAZADO,
        PENDIENTE
    }

    private final Set<String> aceptados;
    private final Set<String> conReparos;
    private final Set<String> rechazados;

    public SiiDteStatusMapper(
            @Value("${sii.estado.aceptados:EPR,DOK}") String aceptados,
            @Value("${sii.estado.aceptados-reparos:}") String conReparos,
            @Value("${sii.estado.rechazados:RCT,RFT,RSC,RCI,RCH,RFR,RTC,RCO,FAU}") String rechazados) {
        this.aceptados = parse(aceptados);
        this.conReparos = parse(conReparos);
        this.rechazados = parse(rechazados);
    }

    public SiiDocumentStatus map(SiiDteQueryResult result) {
        if (result == null) {
            return SiiDocumentStatus.PENDIENTE;
        }
        String estado = normalize(result.estado());
        if (estado == null) {
            return SiiDocumentStatus.PENDIENTE;
        }
        if (conReparos.contains(estado)) {
            return SiiDocumentStatus.ACEPTADO_CON_REPAROS;
        }
        if (aceptados.contains(estado)) {
            String glosa = result.glosa() != null ? result.glosa().toUpperCase() : "";
            return glosa.contains("REPARO")
                    ? SiiDocumentStatus.ACEPTADO_CON_REPAROS
                    : SiiDocumentStatus.ACEPTADO;
        }
        if (rechazados.contains(estado)) {
            return SiiDocumentStatus.RECHAZADO;
        }
        return SiiDocumentStatus.PENDIENTE;
    }

    private static String normalize(String estado) {
        return estado == null ? null : estado.trim().toUpperCase();
    }

    private static Set<String> parse(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }
}
