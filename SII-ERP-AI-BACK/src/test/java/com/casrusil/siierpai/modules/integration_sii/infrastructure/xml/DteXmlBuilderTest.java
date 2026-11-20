package com.casrusil.siierpai.modules.integration_sii.infrastructure.xml;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que el DTE generado tenga la estructura exigida por el SII.
 *
 * <p>
 * No se valida contra el XSD oficial porque el esquema no está disponible en el
 * repositorio (ver informe del agente E). Se verifica estructura, jerarquía y
 * orden de los bloques.
 * </p>
 */
class DteXmlBuilderTest {

    private static final String TED = "<TED version=\"1.0\"><DD/><FRMT>firma</FRMT></TED>";

    private final DteXmlBuilder builder = new DteXmlBuilder();

    @Test
    void buildDte_shouldEmitEncabezadoDetalleTedAndTmstFirmaInOrder() throws Exception {
        Invoice invoice = invoice(InvoiceType.FACTURA_ELECTRONICA, 123L,
                new BigDecimal("1000"), new BigDecimal("190"), new BigDecimal("1190"),
                List.of(
                        new InvoiceLine(1, "Asesoría", null, new BigDecimal("2"), new BigDecimal("500"),
                                new BigDecimal("1000"), "UN"),
                        new InvoiceLine(2, "Soporte", null, BigDecimal.ONE, new BigDecimal("190"),
                                new BigDecimal("190"), "UN")));

        Document doc = parse(builder.buildDte(invoice, TED));

        Element dte = doc.getDocumentElement();
        assertEquals("DTE", dte.getLocalName());
        assertEquals("http://www.sii.cl/SiiDte", dte.getNamespaceURI());

        Element documento = first(doc, "Documento");
        assertEquals("DTE_123", documento.getAttribute("ID"));

        assertEnCabezado(doc, invoice);
        assertEquals(2, children(first(doc, "Documento"), "Detalle").size());
        assertEquals("1", text(first(doc, "Detalle"), "NroLinDet"));
        assertEquals("Asesoría", text(first(doc, "Detalle"), "NmbItem"));

        assertTrue(builder.buildDte(invoice, TED).contains("<TED version=\"1.0\">"));
        assertTrue(first(doc, "TmstFirma").getTextContent().length() > 0, "TmstFirma vacío");
    }

    @Test
    void buildDte_shouldEmitDocumentoBeforeDetalleBeforeTEDBeforeTmstFirma() {
        Invoice invoice = invoice(InvoiceType.FACTURA_ELECTRONICA, 7L,
                new BigDecimal("100"), new BigDecimal("19"), new BigDecimal("119"),
                List.of(new InvoiceLine(1, "Item", null, BigDecimal.ONE, new BigDecimal("100"),
                        new BigDecimal("100"), "UN")));

        String xml = builder.buildDte(invoice, TED);

        int documento = xml.indexOf("<Documento ID=\"DTE_7\">");
        int encabezado = xml.indexOf("<Encabezado>");
        int detalle = xml.indexOf("<Detalle>");
        int ted = xml.indexOf("<TED");
        int tmst = xml.indexOf("<TmstFirma>");
        int cierreDocumento = xml.indexOf("</Documento>");
        int cierreDte = xml.indexOf("</DTE>");

        assertTrue(documento >= 0, "Falta <Documento ID=\"DTE_7\">");
        assertTrue(documento < encabezado && encabezado < detalle, "Orden Encabezado/Detalle inválido");
        assertTrue(detalle < ted && ted < tmst, "El TED debe ir después de Detalle y antes de TmstFirma");
        assertTrue(tmst < cierreDocumento && cierreDocumento < cierreDte, "Cierres desbalanceados");
    }

    @Test
    void buildDte_shouldOmitIvaForExemptDocuments() throws Exception {
        Invoice invoice = invoice(InvoiceType.FACTURA_NO_AFECTA_O_EXENTA_ELECTRONICA, 9L,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("1000"),
                List.of(new InvoiceLine(1, "Exento", null, BigDecimal.ONE, new BigDecimal("1000"),
                        new BigDecimal("1000"), "UN")));

        Document doc = parse(builder.buildDte(invoice, TED));
        Element totales = first(doc, "Totales");

        assertEquals("1000", text(totales, "MntNeto"));
        assertEquals("1000", text(totales, "MntTotal"));
        assertEquals(0, children(totales, "IVA").size());
        assertEquals(0, children(totales, "TasaIVA").size());
    }

    @Test
    void buildDte_shouldEscapeTextValues() throws Exception {
        Invoice invoice = invoice(InvoiceType.FACTURA_ELECTRONICA, 11L,
                new BigDecimal("100"), new BigDecimal("19"), new BigDecimal("119"),
                List.of(new InvoiceLine(1, "Servicio <A&B>", null, BigDecimal.ONE, new BigDecimal("100"),
                        new BigDecimal("100"), "UN")));

        Document doc = parse(builder.buildDte(invoice, TED));

        assertEquals("Servicio <A&B>", text(first(doc, "Detalle"), "NmbItem"));
    }

    private void assertEnCabezado(Document doc, Invoice invoice) {
        Element idDoc = first(doc, "IdDoc");
        assertEquals("33", text(idDoc, "TipoDTE"));
        assertEquals("123", text(idDoc, "Folio"));
        assertEquals(invoice.getDate().toString(), text(idDoc, "FchEmis"));

        Element emisor = first(doc, "Emisor");
        assertEquals(invoice.getIssuerRut(), text(emisor, "RUTEmisor"));
        assertEquals(invoice.getBusinessName(), text(emisor, "RznSoc"));

        Element receptor = first(doc, "Receptor");
        assertEquals(invoice.getReceiverRut(), text(receptor, "RUTRecep"));

        Element totales = first(doc, "Totales");
        assertEquals("1000", text(totales, "MntNeto"));
        assertEquals("19", text(totales, "TasaIVA"));
        assertEquals("190", text(totales, "IVA"));
        assertEquals("1190", text(totales, "MntTotal"));
    }

    private Invoice invoice(InvoiceType type, Long folio, BigDecimal net, BigDecimal tax, BigDecimal total,
            List<InvoiceLine> items) {
        return Invoice.create(
                CompanyId.random(),
                type,
                folio,
                "76123456-7",
                "76987654-3",
                "Emisor SpA",
                LocalDate.of(2026, 11, 17),
                net,
                tax,
                total,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Invoice.ORIGIN_MANUAL,
                TransactionType.SALE,
                items);
    }

    private Document parse(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
    }

    private Element first(Document doc, String localName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        assertTrue(nodes.getLength() > 0, "Falta el elemento " + localName);
        return (Element) nodes.item(0);
    }

    private String text(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        assertTrue(nodes.getLength() > 0, "Falta " + localName + " dentro de " + parent.getLocalName());
        return nodes.item(0).getTextContent();
    }

    private java.util.List<Element> children(Element parent, String localName) {
        java.util.List<Element> result = new java.util.ArrayList<>();
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add((Element) nodes.item(i));
        }
        return result;
    }
}
