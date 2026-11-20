package com.casrusil.siierpai.modules.integration_sii.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.exception.DteEmissionValidationException;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Valida que existan los datos que el esquema del SII exige antes de firmar.
 *
 * <p>
 * Fuente de los requerimientos: {@code DTE_v10.xsd} (SII), bloques
 * {@code Emisor} y {@code Receptor}:
 * </p>
 * <ul>
 * <li>Emisor: RUTEmisor, RznSoc, GiroEmis y Acteco.</li>
 * <li>Receptor: RUTRecep y RznSocRecep.</li>
 * </ul>
 *
 * <p>
 * Falla antes de construir el TED/XML y antes de tomar el certificado, para no
 * dejar una firma a medias ni consumir un folio.
 * </p>
 */
@Component
public class DteEmissionValidator {

    private final CompanyRepository companyRepository;

    public DteEmissionValidator(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public void validate(Invoice invoice) {
        List<String> missing = new ArrayList<>();

        if (invoice.getFolio() == null) {
            missing.add("folio");
        }
        if (blank(invoice.getIssuerRut())) {
            missing.add("RUT del emisor");
        }
        if (blank(invoice.getReceiverRut())) {
            missing.add("RUT del receptor");
        }
        if (blank(invoice.getReceiverName())) {
            missing.add("razón social del receptor");
        }

        Company company = companyRepository.findById(invoice.getCompanyId()).orElse(null);
        if (company == null) {
            missing.add("perfil de empresa del emisor");
        } else {
            if (blank(company.getRazonSocial())) {
                missing.add("razón social del emisor");
            }
            if (blank(company.getGiro())) {
                missing.add("giro del emisor");
            }
            if (blank(company.getActeco())) {
                missing.add("acteco del emisor");
            }
        }

        if (!missing.isEmpty()) {
            throw new DteEmissionValidationException(missing);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
