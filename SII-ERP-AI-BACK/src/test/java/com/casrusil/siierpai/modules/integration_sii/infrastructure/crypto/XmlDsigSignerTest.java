package com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import org.apache.xml.security.signature.XMLSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Firma y validación XML-DSig sobre documentos con atributo {@code ID}.
 *
 * <p>
 * Usa el certificado PKCS#12 de fixture ({@code /fixtures/dummy.p12},
 * password {@code password}) en lugar de mocks, porque
 * {@code XMLSignature#addKeyInfo} requiere un certificado real codificable.
 * </p>
 */
class XmlDsigSignerTest {

    private static final String DSIG_NS = "http://www.w3.org/2000/09/xmldsig#";

    private static final String FIXTURE = "/fixtures/dummy.p12";
    private static final String PASSWORD = "password";

    private static final String DTE_XML = "<DTE version=\"1.0\" xmlns=\"http://www.sii.cl/SiiDte\">"
            + "<Documento ID=\"DTE_123\"><Encabezado><IdDoc><Folio>123</Folio></IdDoc></Encabezado></Documento>"
            + "</DTE>";

    private XmlDsigSigner xmlDsigSigner;
    private SiiCertificate certificate;

    @BeforeEach
    void setUp() {
        xmlDsigSigner = new XmlDsigSigner();
        certificate = loadFixtureCertificate();
    }

    @Test
    void signXml_shouldSignDocumentByReference() throws Exception {
        String signedXml = xmlDsigSigner.signXml(DTE_XML, "DTE_123", certificate);

        assertNotNull(signedXml);
        Document doc = parse(signedXml);
        NodeList signatures = doc.getElementsByTagNameNS(DSIG_NS, "Signature");
        assertEquals(1, signatures.getLength());

        Element signatureElement = (Element) signatures.item(0);
        Element reference = (Element) signatureElement
                .getElementsByTagNameNS(DSIG_NS, "Reference").item(0);
        assertEquals("#DTE_123", reference.getAttribute("URI"));
    }

    @Test
    void signedDte_shouldValidateAgainstPublicKey() throws Exception {
        String signedXml = xmlDsigSigner.signXml(DTE_XML, "DTE_123", certificate);

        assertTrue(validateSignatures(signedXml), "La firma del DTE no valida con la clave pública");
    }

    @Test
    void signedSetDte_shouldValidateAgainstPublicKey() throws Exception {
        String envioXml = "<EnvioDTE xmlns=\"http://www.sii.cl/SiiDte\" version=\"1.0\">"
                + "<SetDTE ID=\"SetDoc\"><Caratula version=\"1.0\"><RutEmisor>76123456-7</RutEmisor></Caratula>"
                + "</SetDTE></EnvioDTE>";

        String signedXml = xmlDsigSigner.signXml(envioXml, "SetDoc", certificate);

        Document doc = parse(signedXml);
        Element signature = (Element) doc.getElementsByTagNameNS(DSIG_NS, "Signature").item(0);
        Element reference = (Element) signature.getElementsByTagNameNS(DSIG_NS, "Reference").item(0);
        assertEquals("#SetDoc", reference.getAttribute("URI"));
        assertTrue(validateSignatures(signedXml), "La firma del SetDTE no valida con la clave pública");
    }

    @Test
    void signXml_withEmptyReference_shouldStillSignWholeDocument() throws Exception {
        String signedXml = xmlDsigSigner.signXml(DTE_XML, "", certificate);

        Document doc = parse(signedXml);
        Element signature = (Element) doc.getElementsByTagNameNS(DSIG_NS, "Signature").item(0);
        Element reference = (Element) signature.getElementsByTagNameNS(DSIG_NS, "Reference").item(0);
        assertEquals("", reference.getAttribute("URI"));
        assertTrue(validateSignatures(signedXml));
    }

    private boolean validateSignatures(String signedXml) throws Exception {
        Document doc = parse(signedXml);
        // Al validar hay que volver a registrar los atributos ID.
        XmlDsigSigner.registerIdAttributes(doc);

        NodeList signatures = doc.getElementsByTagNameNS(DSIG_NS, "Signature");
        assertTrue(signatures.getLength() > 0, "El XML firmado no contiene Signature");

        for (int i = 0; i < signatures.getLength(); i++) {
            Element signatureElement = (Element) signatures.item(i);
            XMLSignature signature = new XMLSignature(signatureElement, "");
            if (!signature.checkSignatureValue(certificate.certificate().getPublicKey())) {
                return false;
            }
        }
        return true;
    }

    private Document parse(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes("ISO-8859-1")));
    }

    private SiiCertificate loadFixtureCertificate() {
        try (InputStream is = getClass().getResourceAsStream(FIXTURE)) {
            assertNotNull(is, "Falta el certificado de prueba " + FIXTURE);
            return new Pkcs12Handler().loadCertificate(is, PASSWORD);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar el certificado de prueba", e);
        }
    }
}
