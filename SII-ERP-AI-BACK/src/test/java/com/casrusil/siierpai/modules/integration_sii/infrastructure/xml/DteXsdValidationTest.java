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
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Valida el DTE firmado contra el esquema oficial del SII
 * ({@code DTE_v10.xsd}, con {@code SiiTypes_v10.xsd} y
 * {@code xmldsignature_v10.xsd}).
 *
 * <p>
 * Los XSD se descargaron desde
 * {@code https://www.sii.cl/factura_electronica/factura_mercado/} y se
 * incluyeron en {@code src/test/resources/xsd/}.
 * </p>
 */
class DteXsdValidationTest {

    private static final String FIXTURE = "/fixtures/dummy.p12";
    private static final String PASSWORD = "password";

    private XmlDsigSigner signer;
    private DteXmlBuilder dteXmlBuilder;
    private TedGenerator tedGenerator;
    private SiiCertificate certificate;
    private CompanyId companyId;

    @BeforeEach
    void setUp() {
        companyId = CompanyId.random();
        Company company = Mockito.mock(Company.class);
        Mockito.when(company.getRazonSocial()).thenReturn("Emisor SpA");
        Mockito.when(company.getGiro()).thenReturn("Servicios de informática");
        Mockito.when(company.getActeco()).thenReturn("620100");
        Mockito.when(company.getCommercialAddress()).thenReturn("Av. Siempre Viva 123");
        Mockito.when(company.getComuna()).thenReturn("Santiago");

        CompanyRepository companyRepository = Mockito.mock(CompanyRepository.class);
        Mockito.when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        signer = new XmlDsigSigner();
        dteXmlBuilder = new DteXmlBuilder(companyRepository);
        tedGenerator = new TedGenerator();
        certificate = loadCertificate();
    }

    @Test
    void signedDteShouldValidateAgainstOfficialXsd() throws Exception {
        Invoice invoice = Invoice.create(
                companyId,
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
                        new BigDecimal("1000"), new BigDecimal("1000"), "UN")))
                .withReceiverName("Receptor SpA");

        String tedXml = tedGenerator.generateTedXml(invoice, caf());
        String dteXml = dteXmlBuilder.buildDte(invoice, tedXml);
        String signedDte = signer.signXml(dteXml, DteXmlBuilder.documentId(invoice), certificate);

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(getClass().getResource("/xsd/DTE_v10.xsd"));

        assertDoesNotThrow(() -> schema.newValidator()
                .validate(new StreamSource(new StringReader(signedDte))));
    }

    private Caf caf() {
        // CAF con la estructura que exige el XSD dentro del TED:
        // DA (RE, RS, TD, RNG, FA, IDK) + FRMA.
        String cafXml = "<CAF version=\"1.0\"><DA><RE>76123456-7</RE><RS>Emisor SpA</RS><TD>33</TD>"
                + "<RNG><D>1</D><H>1000</H></RNG><FA>2026-01-01</FA>"
                + "<RSAPK><M>MIIB</M><E>AQAB</E></RSAPK><IDK>100</IDK></DA>"
                + "<FRMA algoritmo=\"SHA1withRSA\">AQAB</FRMA></CAF>";
        return new Caf(cafXml, 1L, 1000L, certificate.privateKey(), "33",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
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
