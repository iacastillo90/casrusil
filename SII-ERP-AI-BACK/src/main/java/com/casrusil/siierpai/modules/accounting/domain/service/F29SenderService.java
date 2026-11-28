package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Envío de la declaración F29 al SII.
 *
 * <p>
 * El envío electrónico del F29 no está implementado (requiere firma y el
 * servicio del SII). Antes este método simulaba el envío y devolvía un número
 * de transacción inventado, lo que hacía creer al usuario que la declaración
 * había llegado al SII. Ahora falla de forma explícita para que el controlador
 * responda 501 y el contribuyente declare en sii.cl.
 */
@Service
public class F29SenderService {

    private static final Logger log = LoggerFactory.getLogger(F29SenderService.class);

    static final String NOT_IMPLEMENTED_MESSAGE =
            "El envío del F29 al SII no está implementado. Declare el formulario en sii.cl.";

    /**
     * @param companyId empresa
     * @param report    F29 calculado
     * @return nunca retorna
     * @throws UnsupportedOperationException siempre: el envío real no existe
     */
    public String sendDeclaration(CompanyId companyId, F29Report report) {
        log.warn("Intento de envío de F29 (empresa {}, período {}): envío al SII no implementado",
                companyId.value(), report.period());
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }
}
