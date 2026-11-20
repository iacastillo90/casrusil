package com.casrusil.siierpai.modules.integration_sii.domain.port.out;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiDteQueryResult;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Consulta de estado de envíos y documentos en el SII.
 *
 * <p>
 * Contrato tomado de los WSDL oficiales (maullin):
 * </p>
 * <ul>
 * <li>{@code QueryEstUp.jws}: {@code getEstUp(RutCompania, DvCompania, TrackId,
 * Token)}.</li>
 * <li>{@code QueryEstDte.jws}: {@code getEstDte(RutConsultante, DvConsultante,
 * RutCompania, DvCompania, RutReceptor, DvReceptor, TipoDte, FolioDte,
 * FechaEmisionDte, MontoDte, Token)}.</li>
 * </ul>
 *
 * <p>
 * Los RUT se envían sin dígito verificador y el DV por separado.
 * </p>
 */
public interface SiiDteStatusClient {

    SiiDteQueryResult queryEnvio(String token, String rutCompania, String dvCompania, String trackId);

    SiiDteQueryResult queryDte(String token,
            String rutConsultante, String dvConsultante,
            String rutCompania, String dvCompania,
            String rutReceptor, String dvReceptor,
            int tipoDte, long folio, LocalDate fechaEmision, BigDecimal monto);
}
