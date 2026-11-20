package com.casrusil.siierpai.modules.payroll.infrastructure.adapter;

import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.entity.TaxParameterEntity;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.repository.TaxParameterJpaRepository;
import com.casrusil.siierpai.modules.payroll.domain.port.TaxParameterProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resuelve UF/UTM desde los parámetros tributarios anuales ya cargados
 * ({@code tax_parameters}, clave {@code YEAR_<año>}).
 */
@Component
@RequiredArgsConstructor
public class PayrollTaxParameterAdapter implements TaxParameterProviderPort {

    private final TaxParameterJpaRepository taxParameterRepository;

    @Override
    public Optional<TaxParameters> findForYear(int year) {
        return taxParameterRepository.findById("YEAR_" + year)
                .filter(entity -> entity.getUfValue() != null && entity.getUtmValue() != null)
                .map(this::toParameters);
    }

    private TaxParameters toParameters(TaxParameterEntity entity) {
        return new TaxParameters(entity.getUfValue(), entity.getUtmValue());
    }
}
