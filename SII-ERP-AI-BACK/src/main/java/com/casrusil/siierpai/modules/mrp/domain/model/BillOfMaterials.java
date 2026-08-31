package com.casrusil.siierpai.modules.mrp.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BillOfMaterials(
    UUID id,
    CompanyId companyId,
    String productId,
    String name,
    List<BomItem> items
) {
    public record BomItem(String componentProductId, BigDecimal quantity) {}
}