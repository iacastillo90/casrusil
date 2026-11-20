package com.casrusil.siierpai.modules.fixed_assets.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.fixed_assets.application.service.FixedAssetServiceImpl;
import com.casrusil.siierpai.modules.fixed_assets.domain.model.FixedAsset;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FixedAssetByIdEndpointTest {

    private final FixedAssetServiceImpl service = mock(FixedAssetServiceImpl.class);
    private final FixedAssetController controller = new FixedAssetController(service);

    @Test
    void getAssetShouldReturnTheRequestedAsset() throws Exception {
        CompanyId companyId = CompanyId.random();
        UUID id = UUID.randomUUID();
        FixedAsset asset = mock(FixedAsset.class);
        when(service.getAsset(any(), any())).thenReturn(asset);

        var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(() -> controller.getAsset(id));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(asset, response.getBody());
    }
}
