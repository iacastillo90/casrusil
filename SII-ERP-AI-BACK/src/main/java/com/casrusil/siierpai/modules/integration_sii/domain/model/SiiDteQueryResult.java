package com.casrusil.siierpai.modules.integration_sii.domain.model;

/**
 * Respuesta de los web services de consulta del SII
 * ({@code QueryEstUp.jws} / {@code QueryEstDte.jws}).
 *
 * <p>
 * El servicio devuelve un XML embebido como string con esta forma (observada
 * contra maullin):
 * </p>
 *
 * <pre>
 * &lt;SII:RESPUESTA xmlns:SII="http://www.sii.cl/XMLSchema"&gt;
 *   &lt;SII:RESP_HDR&gt;&lt;ESTADO&gt;001&lt;/ESTADO&gt;&lt;GLOSA&gt;TOKEN NO EXISTE&lt;/GLOSA&gt;&lt;/SII:RESP_HDR&gt;
 * &lt;/SII:RESPUESTA&gt;
 * </pre>
 *
 * @param headerEstado  ESTADO de RESP_HDR (estado de la consulta)
 * @param headerGlosa   GLOSA de RESP_HDR
 * @param bodyEstado    ESTADO de RESP_BODY (estado del envío/documento), si viene
 * @param bodyGlosa     GLOSA de RESP_BODY, si viene
 * @param raw           XML original devuelto por el SII
 */
public record SiiDteQueryResult(
        String headerEstado,
        String headerGlosa,
        String bodyEstado,
        String bodyGlosa,
        String raw) {

    /**
     * Estado más específico disponible: el del cuerpo si existe, si no el del
     * encabezado.
     */
    public String estado() {
        return bodyEstado != null && !bodyEstado.isBlank() ? bodyEstado : headerEstado;
    }

    public String glosa() {
        return bodyGlosa != null && !bodyGlosa.isBlank() ? bodyGlosa : headerGlosa;
    }
}
