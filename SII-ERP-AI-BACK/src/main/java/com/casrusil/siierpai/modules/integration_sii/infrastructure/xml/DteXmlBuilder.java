package com.casrusil.siierpai.modules.integration_sii.infrastructure.xml;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Constructor de XML para Documentos Tributarios Electrónicos (DTE).
 *
 * <p>
 * Genera la estructura del DTE según el formato del SII:
 * </p>
 *
 * <pre>
 * DTE
 *  └─ Documento ID="DTE_{folio}"
 *      ├─ Encabezado
 *      │   ├─ IdDoc (TipoDTE, Folio, FchEmis)
 *      │   ├─ Emisor (RUTEmisor, RznSoc, GiroEmis, Acteco, DirOrigen, CmnaOrigen)
 *      │   ├─ Receptor (RUTRecep, RznSocRecep, DirRecep, CmnaRecep)
 *      │   └─ Totales (MntNeto, TasaIVA, IVA, MntTotal)
 *      ├─ Detalle (uno por línea)
 *      ├─ TED
 *      └─ TmstFirma
 * </pre>
 *
 * <p>
 * Los datos de emisor/receptor que no existen en el agregado {@link Invoice}
 * (giro, acteco, direcciones y comuna) se emiten sólo si están configurados en
 * {@code sii.emisor.*} / {@code sii.receptor.*}. El maestro de empresas debe
 * proveerlos antes de emitir contra el SII; ver informe del agente E.
 * </p>
 *
 * @see XmlDsigSigner
 * @see TedGenerator
 * @since 1.0
 */
@Component
public class DteXmlBuilder {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${sii.emisor.giro:}")
    private String emisorGiro;

    @Value("${sii.emisor.acteco:}")
    private String emisorActeco;

    @Value("${sii.emisor.direccion:}")
    private String emisorDireccion;

    @Value("${sii.emisor.comuna:}")
    private String emisorComuna;

    @Value("${sii.receptor.direccion:}")
    private String receptorDireccion;

    @Value("${sii.receptor.comuna:}")
    private String receptorComuna;

    private final CompanyRepository companyRepository;

    public DteXmlBuilder(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Identificador XML del documento. Debe coincidir con la referencia que se
     * firma ({@link XmlDsigSigner}) y con el {@code ID} del elemento
     * {@code Documento}.
     *
     * @param invoice factura a emitir
     * @return identificador con el formato {@code DTE_{folio}}
     */
    public static String documentId(Invoice invoice) {
        return "DTE_" + invoice.getFolio();
    }

    public String buildDte(Invoice invoice, String tedXml) {
        Company company = companyRepository.findById(invoice.getCompanyId()).orElse(null);
        StringBuilder sb = new StringBuilder();

        // Sin declaración XML: el DTE se embebe dentro de <EnvioDTE>, que ya la
        // incluye. Una segunda declaración invalidaría el sobre.
        sb.append("<DTE version=\"1.0\" xmlns=\"http://www.sii.cl/SiiDte\">");
        sb.append("<Documento ID=\"").append(escape(documentId(invoice))).append("\">");

        sb.append("<Encabezado>");
        appendIdDoc(sb, invoice);
        appendEmisor(sb, invoice, company);
        appendReceptor(sb, invoice, company);
        appendTotales(sb, invoice);
        sb.append("</Encabezado>");

        appendDetalle(sb, invoice);

        // El TED ya viene como <TED>...</TED> desde TedGenerator.
        sb.append(tedXml != null ? tedXml : "");

        sb.append("<TmstFirma>")
                .append(java.time.LocalDateTime.now().format(TIMESTAMP))
                .append("</TmstFirma>");

        sb.append("</Documento>");
        sb.append("</DTE>");

        return sb.toString();
    }

    private void appendIdDoc(StringBuilder sb, Invoice invoice) {
        sb.append("<IdDoc>");
        sb.append("<TipoDTE>").append(invoice.getType().getCode()).append("</TipoDTE>");
        sb.append("<Folio>").append(invoice.getFolio()).append("</Folio>");
        sb.append("<FchEmis>").append(invoice.getDate().format(DateTimeFormatter.ISO_DATE)).append("</FchEmis>");
        sb.append("</IdDoc>");
    }

    private void appendEmisor(StringBuilder sb, Invoice invoice, Company company) {
        sb.append("<Emisor>");
        sb.append("<RUTEmisor>").append(escape(invoice.getIssuerRut())).append("</RUTEmisor>");

        // Prioridad: perfil de empresa; fallback a sii.emisor.* en properties.
        String razonSocial = firstNonBlank(
                company != null ? company.getRazonSocial() : null,
                invoice.getBusinessName());
        if (notBlank(razonSocial)) {
            sb.append("<RznSoc>").append(escape(razonSocial)).append("</RznSoc>");
        }
        appendIfPresent(sb, "GiroEmis", firstNonBlank(company != null ? company.getGiro() : null, emisorGiro));
        appendIfPresent(sb, "Acteco", firstNonBlank(company != null ? company.getActeco() : null, emisorActeco));
        appendIfPresent(sb, "DirOrigen",
                firstNonBlank(company != null ? company.getCommercialAddress() : null, emisorDireccion));
        appendIfPresent(sb, "CmnaOrigen", firstNonBlank(company != null ? company.getComuna() : null, emisorComuna));
        sb.append("</Emisor>");
    }

    private void appendReceptor(StringBuilder sb, Invoice invoice, Company company) {
        sb.append("<Receptor>");
        sb.append("<RUTRecep>").append(escape(invoice.getReceiverRut())).append("</RUTRecep>");
        // RznSocRecep es obligatoria en el esquema.
        appendIfPresent(sb, "RznSocRecep", invoice.getReceiverName());
        appendIfPresent(sb, "DirRecep", receptorDireccion);
        appendIfPresent(sb, "CmnaRecep", receptorComuna);
        sb.append("</Receptor>");
    }

    private void appendTotales(StringBuilder sb, Invoice invoice) {
        sb.append("<Totales>");
        sb.append("<MntNeto>").append(invoice.getNetAmount()).append("</MntNeto>");
        // Los documentos exentos (34) no llevan tasa ni monto de IVA.
        boolean afecto = invoice.getTaxAmount() != null
                && invoice.getTaxAmount().signum() > 0;
        if (afecto) {
            sb.append("<TasaIVA>").append(tasaIva(invoice)).append("</TasaIVA>");
            sb.append("<IVA>").append(invoice.getTaxAmount()).append("</IVA>");
        }
        sb.append("<MntTotal>").append(invoice.getTotalAmount()).append("</MntTotal>");
        sb.append("</Totales>");
    }

    /**
     * Tasa de IVA vigente. Se deriva de la relación IVA/neto del propio
     * documento (19 para documentos afectos), evitando leer un período fijo.
     */
    private String tasaIva(Invoice invoice) {
        if (invoice.getNetAmount() == null || invoice.getNetAmount().signum() == 0) {
            return "19";
        }
        return invoice.getTaxAmount()
                .multiply(java.math.BigDecimal.valueOf(100))
                .divide(invoice.getNetAmount(), 0, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }

    private void appendDetalle(StringBuilder sb, Invoice invoice) {
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            sb.append("<Detalle>");
            sb.append("<NroLinDet>1</NroLinDet>");
            sb.append("<NmbItem>").append(escape(invoice.getItemsSummary())).append("</NmbItem>");
            sb.append("<MontoItem>").append(invoice.getNetAmount()).append("</MontoItem>");
            sb.append("</Detalle>");
            return;
        }

        int lineNumber = 1;
        for (InvoiceLine line : invoice.getItems()) {
            sb.append("<Detalle>");
            sb.append("<NroLinDet>").append(lineNumber++).append("</NroLinDet>");
            if (notBlank(line.itemCode())) {
                sb.append("<CdgItem><TpoCodigo>INT1</TpoCodigo><VlrCodigo>")
                        .append(escape(line.itemCode())).append("</VlrCodigo></CdgItem>");
            }
            sb.append("<NmbItem>").append(escape(line.itemName())).append("</NmbItem>");
            sb.append("<QtyItem>").append(line.quantity()).append("</QtyItem>");
            sb.append("<PrcItem>").append(line.price()).append("</PrcItem>");
            java.math.BigDecimal amount = line.amount() != null
                    ? line.amount()
                    : line.quantity().multiply(line.price());
            sb.append("<MontoItem>").append(amount).append("</MontoItem>");
            sb.append("</Detalle>");
        }
    }

    private void appendIfPresent(StringBuilder sb, String tag, String value) {
        if (notBlank(value)) {
            sb.append("<").append(tag).append(">").append(escape(value)).append("</").append(tag).append(">");
        }
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return notBlank(preferred) ? preferred : fallback;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
