package com.casrusil.siierpai.modules.pos.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.inventory.domain.model.Product;
import com.casrusil.siierpai.modules.inventory.domain.port.ProductRepositoryPort;
import com.casrusil.siierpai.modules.pos.domain.port.in.PosUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PosProductSearchEndpointTest {

    private final PosUseCase posUseCase = mock(PosUseCase.class);
    private final ProductRepositoryPort productRepository = mock(ProductRepositoryPort.class);
    private final PosController controller = new PosController(posUseCase, productRepository);

    @Test
    void searchShouldReturnItemBySku() throws Exception {
        CompanyId companyId = CompanyId.random();
        Product product = Product.builder()
                .id(5L).companyId(companyId.value().toString()).sku("SKU-1")
                .name("Café molido").salePrice(new BigDecimal("2990")).build();
        when(productRepository.findAllByCompanyId(anyString())).thenReturn(List.of(product));

        var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(() -> controller.searchProduct("sku-1"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("5", response.getBody().productId());
        assertEquals("Café molido", response.getBody().productName());
        assertEquals(new BigDecimal("2990"), response.getBody().unitPrice());
    }

    @Test
    void searchShouldReturn404WhenNoProductMatches() throws Exception {
        CompanyId companyId = CompanyId.random();
        when(productRepository.findAllByCompanyId(anyString())).thenReturn(List.of());

        var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(() -> controller.searchProduct("nada"));

        assertEquals(404, response.getStatusCode().value());
    }
}
