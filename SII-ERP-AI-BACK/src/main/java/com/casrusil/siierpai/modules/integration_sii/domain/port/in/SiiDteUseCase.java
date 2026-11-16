package com.casrusil.siierpai.modules.integration_sii.domain.port.in;

import com.casrusil.siierpai.modules.integration_sii.domain.model.DteSendResult;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

/**
 * Inbound port for DTE status lookups and DTE submission to the SII.
 */
public interface SiiDteUseCase {

    /**
     * Finds a DTE by SII type and folio within the current company.
     *
     * @param tipo numeric code or {@code InvoiceType} name
     * @param folio folio as received from the request
     */
    Invoice getDte(CompanyId companyId, String tipo, String folio);

    /**
     * Sends one existing invoice to the SII.
     *
     * @param dteId invoice UUID as received from the request
     */
    DteSendResult sendDte(CompanyId companyId, String dteId);
}
