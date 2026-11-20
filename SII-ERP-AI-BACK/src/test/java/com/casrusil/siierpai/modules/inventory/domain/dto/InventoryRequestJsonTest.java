package com.casrusil.siierpai.modules.inventory.domain.dto;

import com.casrusil.siierpai.modules.inventory.domain.model.MovementType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Contrato JSON de los request de inventario.
 *
 * <p>Reproduce el payload exacto que envía el front
 * (`POST /inventory/products/{id}/movements`) para evitar el 400
 * "Malformed JSON request" que provocaba el DTO sin constructor por defecto.</p>
 */
class InventoryRequestJsonTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void stockMovementAcceptsFrontPayloadWithSpanishType() throws Exception {
        String json = """
                {"productId":1,"type":"ENTRADA","quantity":2}
                """;

        StockMovementRequest request = mapper.readValue(json, StockMovementRequest.class);

        assertNotNull(request);
        assertEquals(1L, request.getProductId());
        assertEquals(MovementType.IN, request.getType());
        assertEquals(new BigDecimal("2"), request.getQuantity());
    }

    @Test
    void stockMovementAcceptsSalidaAndEnglishValues() throws Exception {
        assertEquals(MovementType.OUT,
                mapper.readValue("{\"type\":\"SALIDA\",\"quantity\":1}", StockMovementRequest.class).getType());
        assertEquals(MovementType.ADJUSTMENT,
                mapper.readValue("{\"type\":\"AJUSTE\",\"quantity\":1}", StockMovementRequest.class).getType());
        assertEquals(MovementType.IN,
                mapper.readValue("{\"type\":\"IN\",\"quantity\":1}", StockMovementRequest.class).getType());
    }

    @Test
    void stockMovementAcceptsOptionalFields() throws Exception {
        String json = """
                {
                  "productId": 7,
                  "type": "SALIDA",
                  "quantity": 1.5,
                  "unitCost": 1200,
                  "movementDate": "2026-11-20T10:00:00",
                  "referenceDocument": "GUIA-1",
                  "description": "Merma"
                }
                """;

        StockMovementRequest request = mapper.readValue(json, StockMovementRequest.class);

        assertEquals(new BigDecimal("1200"), request.getUnitCost());
        assertEquals("GUIA-1", request.getReferenceDocument());
        assertNotNull(request.getMovementDate());
    }

    @Test
    void productRequestDeserializesFrontPayload() throws Exception {
        String json = """
                {"sku":"SKU-1","name":"Cable","category":"Insumos","salePrice":1500,"minStock":5}
                """;

        ProductRequest request = mapper.readValue(json, ProductRequest.class);

        assertEquals("SKU-1", request.getSku());
        assertEquals(new BigDecimal("1500"), request.getSalePrice());
        assertEquals(new BigDecimal("5"), request.getMinStock());
    }
}
