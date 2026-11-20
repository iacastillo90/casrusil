package com.casrusil.siierpai.modules.payroll.domain.port;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Puerto para obtener los parámetros tributarios del año (UF y UTM).
 * Permite crear un período sin exigir que el usuario digite los valores.
 */
public interface TaxParameterProviderPort {

    Optional<TaxParameters> findForYear(int year);

    record TaxParameters(BigDecimal ufValue, BigDecimal utmValue) {
    }
}
