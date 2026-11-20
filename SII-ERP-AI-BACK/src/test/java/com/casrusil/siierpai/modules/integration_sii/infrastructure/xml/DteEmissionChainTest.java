package com.casrusil.siierpai.modules.integration_sii.infrastructure.xml;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.Pkcs12Handler;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.TedGenerator;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.XmlDsigSigner;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.apache.xml.security.signature.XMLSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integración de la cadena de emisión completa, sin mocks:
 * construir DTE → timbrar (TED) → firmar DTE → armar sobre → firmar sobre.
 *
 * <p>
 * Usa el certificado PKCS#12 de fixture ({@code /fixtures/dummy.p12}) y una
 * llave RSA real para el TED, por lo que ejercita exactamente el mismo camino
 * que {@code DteSenderService} antes de hablar con el SII.
 * </p>
 */
class DteEmissionChainTest {

    private static final String DSIG_NS = "http://www.w3.org/2000/09/xmldsig#";

    private static final String FIXTURE = "/fixtures/dummy.p12";
    private static final String PASSWORD = "password";

    private XmlDsigSigner signer;
    private DteXmlBuilder dteXmlBuilder;
    private EnvioDteBuilder envioDteBuilder;
    private TedGenerator tedGenerator;
    private SiiCertificate certificate;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        signer = new XmlDsigSigner();
        dteXmlBuilder = new DteXmlBuilder();
        envioDteBuilder = new EnvioDteBuilder();
        tedGenerator = new TedGenerator();
        certificate = loadCertificate();
        invoice = invoice();
    }

    @Test
    void fullChain_shouldProduceEnvelopeWithTwoValidSignatures() throws Exception {
        String tedXml = tedGenerator.generateTedXml(invoice, caf());
        String dteXml = dteXmlBuilder.buildDte(invoice, tedXml);

        String signedDteXml = signer.signXml(dteXml, DteXmlBuilder.documentId(invoice), certificate);
        String envioXml = envioDteBuilder.wrap(signedDteXml, invoice.getCompanyId(),
                invoice.getIssuerRut(), invoice.getIssuerRut());
        String signedEnvioXml = signer.signXml(envioXml, "SetDoc", certificate);

        Document doc = parse(signedEnvioXml);
        XmlDsigSigner.registerIdAttributes(doc);

        NodeList signatures = doc.getElementsByTagNameNS(DSIG_NS, "Signature");
        assertEquals(2, signatures.getLength(), "Debe haber firma del DTE y del sobre");

        for (int i = 0; i < signatures.getLength(); i++) {
            XMLSignature signature = new XMLSignature((Element) signatures.item(i), "");
            assertTrue(signature.checkSignatureValue(certificate.certificate().getPublicKey()),
                    "Firma " + (i + 1) + " inválida tras el ensamblado del sobre");
        }

        Element documento = (Element) doc.getElementsByTagNameNS("*", "Documento").item(0);
        assertEquals("DTE_123", documento.getAttribute("ID"));
        assertTrue(doc.getElementsByTagNameNS("*", "TED").getLength() > 0, "Falta el TED en el DTE");
        assertTrue(doc.getElementsByTagNameNS("*", "TmstFirma").getLength() > 0, "Falta TmstFirma");
        assertTrue(signedEnvioXml.contains("<SetDTE ID=\"SetDoc\">"), "Falta SetDTE con ID SetDoc");
    }

    private Caf caf() {
        String cafXml = "<CAF version=\"1.0\"><DA><RE>76123456-7</RE><TD>33</TD>"
                + "<RNG><D>1</D><H>1000</H></RNG></DA></CAF>";
        return new Caf(cafXml, 1L, 1000L, certificate.privateKey(), "33",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
    }

    private Invoice invoice() {
        return Invoice.create(
                CompanyId.random(),
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76123456-7",
                "76987654-3",
                "Emisor SpA",
                LocalDate.of(2026, 11, 17),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Invoice.ORIGIN_MANUAL,
                TransactionType.SALE,
                List.of(new InvoiceLine(1, "Asesoría", null, BigDecimal.ONE,
                        new BigDecimal("1000"), new BigDecimal("1000"), "UN")));
    }

    private Document parse(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("ISO-8859-1")));
    }

    private SiiCertificate loadCertificate() {
        try (InputStream is = getClass().getResourceAsStream(FIXTURE)) {
            assertNotNull(is, "Falta el certificado de prueba " + FIXTURE);
            return new Pkcs12Handler().loadCertificate(is, PASSWORD);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar el certificado de prueba", e);
        }
    }
}
