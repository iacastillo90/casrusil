package com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Componente de Firma Digital XML (XML-DSig).
 * 
 * <p>
 * Implementa el estándar XML Digital Signature para firmar documentos
 * electrónicos según la normativa del SII.
 * 
 * <p>
 * Características:
 * <ul>
 * <li>Firma Enveloped (la firma queda dentro del documento).</li>
 * <li>Uso de algoritmos RSA-SHA1 (estándar SII).</li>
 * <li>Canonicalización y transformación de referencias.</li>
 * </ul>
 * 
 * @see Pkcs12Handler
 * @since 1.0
 */
@Component
public class XmlDsigSigner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XmlDsigSigner.class);

    public XmlDsigSigner() {
        log.debug("XmlDsigSigner instantiated");
    }

    static {
        try {
            Init.init();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(XmlDsigSigner.class).error("Failed to initialize xmlsec", e);
        }
    }

    public String signXml(String xmlContent, String referenceId, SiiCertificate certificate) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlContent)));

            // Santuario resuelve las referencias "#ID" sólo si el atributo está
            // declarado como tipo ID. Los DTE del SII usan ID="..." en
            // <Documento> y ID="SetDoc" en <SetDTE>, sin DTD/XSD que lo declare.
            registerIdAttributes(doc);

            // SignedInfo también con C14N exclusiva: de lo contrario los
            // namespaces heredados del sobre (xmlns:xsi) cambian el SignedInfo
            // al embeber el DTE y la firma deja de validar.
            XMLSignature signature = new XMLSignature(doc, null, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
                    Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

            // Append signature to document root
            doc.getDocumentElement().appendChild(signature.getElement());

            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            // C14N exclusiva: el SII la usa y es obligatoria para que la firma del
            // DTE siga válida al embeberlo dentro de <EnvioDTE> (los namespaces
            // heredados del sobre no deben entrar en el digest).
            transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

            String uri = (referenceId == null || referenceId.isEmpty()) ? "" : "#" + referenceId;
            signature.addDocument(uri, transforms,
                    org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1);

            signature.addKeyInfo(certificate.certificate());
            signature.addKeyInfo(certificate.certificate().getPublicKey());

            signature.sign(certificate.privateKey());

            return documentToString(doc, xmlContent);

        } catch (Exception e) {
            throw new RuntimeException("Error signing XML", e);
        }
    }

    /**
     * Declara como tipo ID todos los atributos {@code ID} presentes en el
     * documento.
     *
     * <p>
     * Sin este registro, Apache Santuario no puede resolver referencias como
     * {@code #DTE_123} o {@code #SetDoc} y lanza
     * {@code ResourceResolverException}. Es necesario tanto al firmar como al
     * validar la firma.
     * </p>
     *
     * @param doc documento ya parseado (namespace aware)
     */
    public static void registerIdAttributes(Document doc) {
        NodeList elements = doc.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (!(node instanceof Element element) || !element.hasAttribute("ID")) {
                continue;
            }
            try {
                element.setIdAttribute("ID", true);
            } catch (DOMException ignored) {
                // El atributo ya estaba declarado como ID: no es un error.
            }
        }
    }

    private String documentToString(Document doc, String originalXml) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        // CRITICAL: SII requires ISO-8859-1
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "ISO-8859-1");
        // Se preserva la declaración original: el DTE se firma sin ella porque
        // se embebe en <EnvioDTE> (una declaración intermedia invalidaría el
        // sobre), mientras que el sobre sí la conserva.
        boolean hadDeclaration = originalXml != null
                && originalXml.stripLeading().startsWith("<?xml");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION,
                hadDeclaration ? "no" : "yes");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(baos));
        return baos.toString("ISO-8859-1");
    }
}
