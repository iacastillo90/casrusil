package com.casrusil.siierpai.modules.integration_sii.infrastructure.xml;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Constructor de Sobres de Envío DTE (SetDTE).
 * 
 * <p>
 * Empaqueta múltiples DTEs individuales en un único sobre XML (EnvioDte)
 * para su envío al SII.
 * 
 * <p>
 * El sobre incluye:
 * <ul>
 * <li>Carátula con información del envío (Emisor, Receptor, Cantidad de
 * DTEs).</li>
 * <li>Lista de DTEs firmados individualmente.</li>
 * <li>Firma digital del sobre completo.</li>
 * </ul>
 * 
 * @since 1.0
 */
@Component
public class EnvioDteBuilder {

    public String wrap(String signedDteXml, CompanyId companyId, String rutEmisor, String rutEmpresa) {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        // Sin xmlns:xsi ni xsi:schemaLocation: cualquier namespace extra en la
        // raíz cambia el SignedInfo del DTE al embeberlo (C14N inclusiva) y la
        // firma deja de validar. schemaLocation es opcional para el SII.
        sb.append("<EnvioDTE xmlns=\"http://www.sii.cl/SiiDte\" version=\"1.0\">");

        sb.append("<SetDTE ID=\"SetDoc\">");

        sb.append("<Caratula version=\"1.0\">");
        sb.append("<RutEmisor>").append(rutEmisor).append("</RutEmisor>");
        sb.append("<RutEnvia>").append(rutEmpresa).append("</RutEnvia>"); // Usually the same or representative
        sb.append("<RutReceptor>").append("60803000-K").append("</RutReceptor>"); // SII RUT
        sb.append("<FchResol>").append("2014-08-22").append("</FchResol>"); // Resolution Date (Example)
        sb.append("<NroResol>").append("80").append("</NroResol>"); // Resolution Number (Example)
        sb.append("<TmstFirmaEnv>").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .append("</TmstFirmaEnv>");

        // Subtotals
        sb.append("<SubTotDTE>");
        sb.append("<TpoDTE>").append("33").append("</TpoDTE>"); // Example: Factura Electronica
        sb.append("<NroDTE>").append("1").append("</NroDTE>");
        sb.append("</SubTotDTE>");

        sb.append("</Caratula>");

        // Append Signed DTE
        sb.append(signedDteXml);

        sb.append("</SetDTE>");
        sb.append("</EnvioDTE>");

        return sb.toString();
    }
}
