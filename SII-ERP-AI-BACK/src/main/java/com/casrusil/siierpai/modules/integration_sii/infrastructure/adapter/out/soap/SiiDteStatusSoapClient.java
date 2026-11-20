package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.soap;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiDteQueryResult;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiDteStatusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cliente SOAP (RPC/encoded) para QueryEstUp y QueryEstDte.
 *
 * <p>
 * Los servicios se invocan en el ambiente de certificación (maullin) por
 * defecto; la URL es configurable con {@code sii.url.query-est-up} y
 * {@code sii.url.query-est-dte}.
 * </p>
 */
@Component
public class SiiDteStatusSoapClient implements SiiDteStatusClient {

    private static final Pattern RETURN_VALUE = Pattern.compile(
            "<(?:[\\w.-]+:)?getEst(?:Up|Dte)Return[^>]*>(.*?)</(?:[\\w.-]+:)?getEst(?:Up|Dte)Return>",
            Pattern.DOTALL);

    private final HttpClient httpClient;
    private final String queryEstUpUrl;
    private final String queryEstDteUrl;

    @Autowired
    public SiiDteStatusSoapClient(
            @Value("${sii.url.query-est-up:https://maullin.sii.cl/DTEWS/QueryEstUp.jws}") String queryEstUpUrl,
            @Value("${sii.url.query-est-dte:https://maullin.sii.cl/DTEWS/QueryEstDte.jws}") String queryEstDteUrl) {
        this(queryEstUpUrl, queryEstDteUrl, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build());
    }

    SiiDteStatusSoapClient(String queryEstUpUrl, String queryEstDteUrl, HttpClient httpClient) {
        this.queryEstUpUrl = queryEstUpUrl;
        this.queryEstDteUrl = queryEstDteUrl;
        this.httpClient = httpClient;
    }

    @Override
    public SiiDteQueryResult queryEnvio(String token, String rutCompania, String dvCompania, String trackId) {
        String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:def="http://DefaultNamespace" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <def:getEstUp>
                      <RutCompania xsi:type="xsd:string">%s</RutCompania>
                      <DvCompania xsi:type="xsd:string">%s</DvCompania>
                      <TrackId xsi:type="xsd:string">%s</TrackId>
                      <Token xsi:type="xsd:string">%s</Token>
                    </def:getEstUp>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(escape(rutCompania), escape(dvCompania), escape(trackId), escape(token));
        return call(queryEstUpUrl, body);
    }

    @Override
    public SiiDteQueryResult queryDte(String token,
            String rutConsultante, String dvConsultante,
            String rutCompania, String dvCompania,
            String rutReceptor, String dvReceptor,
            int tipoDte, long folio, LocalDate fechaEmision, BigDecimal monto) {
        String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:def="http://DefaultNamespace" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <def:getEstDte>
                      <RutConsultante xsi:type="xsd:string">%s</RutConsultante>
                      <DvConsultante xsi:type="xsd:string">%s</DvConsultante>
                      <RutCompania xsi:type="xsd:string">%s</RutCompania>
                      <DvCompania xsi:type="xsd:string">%s</DvCompania>
                      <RutReceptor xsi:type="xsd:string">%s</RutReceptor>
                      <DvReceptor xsi:type="xsd:string">%s</DvReceptor>
                      <TipoDte xsi:type="xsd:string">%d</TipoDte>
                      <FolioDte xsi:type="xsd:string">%d</FolioDte>
                      <FechaEmisionDte xsi:type="xsd:string">%s</FechaEmisionDte>
                      <MontoDte xsi:type="xsd:string">%s</MontoDte>
                      <Token xsi:type="xsd:string">%s</Token>
                    </def:getEstDte>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(escape(rutConsultante), escape(dvConsultante),
                escape(rutCompania), escape(dvCompania),
                escape(rutReceptor), escape(dvReceptor),
                tipoDte, folio, fechaEmision, monto.toPlainString(), escape(token));
        return call(queryEstDteUrl, body);
    }

    private SiiDteQueryResult call(String url, String soapBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "text/xml;charset=UTF-8")
                    .header("SOAPAction", "\"\"")
                    .POST(HttpRequest.BodyPublishers.ofString(soapBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("SII status query failed with HTTP " + response.statusCode());
            }

            String embedded = extractEmbeddedXml(response.body());
            return parse(embedded, response.body());

        } catch (Exception e) {
            throw new IllegalStateException("Error querying SII status: " + e.getMessage(), e);
        }
    }

    private String extractEmbeddedXml(String soapResponse) {
        Matcher matcher = RETURN_VALUE.matcher(soapResponse);
        if (!matcher.find()) {
            throw new IllegalStateException("Unexpected SII response: missing getEstUpReturn/getEstDteReturn");
        }
        return unescapeXml(matcher.group(1)).trim();
    }

    private SiiDteQueryResult parse(String embeddedXml, String soapResponse) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(embeddedXml.getBytes(StandardCharsets.ISO_8859_1)));

        String headerEstado = text(doc, "RESP_HDR", "ESTADO");
        String headerGlosa = text(doc, "RESP_HDR", "GLOSA");
        String bodyEstado = text(doc, "RESP_BODY", "ESTADO");
        String bodyGlosa = text(doc, "RESP_BODY", "GLOSA");

        return new SiiDteQueryResult(headerEstado, headerGlosa, bodyEstado, bodyGlosa, soapResponse);
    }

    private String text(Document doc, String parentTag, String tag) {
        NodeList parents = doc.getElementsByTagNameNS("*", parentTag);
        if (parents.getLength() == 0) {
            return null;
        }
        NodeList nodes = ((org.w3c.dom.Element) parents.item(0)).getElementsByTagNameNS("*", tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : null;
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String unescapeXml(String value) {
        return value.replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&apos;", "'")
                .replace("&amp;", "&");
    }
}
