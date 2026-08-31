package com.casrusil.siierpai.modules.wms.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.util.UUID;

public class WarehouseLocation {
    private final UUID id;
    private final CompanyId companyId;
    private final String code; // e.g., A-01-02
    private final String description;
    private final String zone; // e.g., FRÍO, SECO

    public WarehouseLocation(UUID id, CompanyId companyId, String code, String description, String zone) {
        this.id = id;
        this.companyId = companyId;
        this.code = code;
        this.description = description;
        this.zone = zone;
    }

    public static WarehouseLocation create(CompanyId companyId, String code, String description, String zone) {
        return new WarehouseLocation(UUID.randomUUID(), companyId, code, description, zone);
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getZone() { return zone; }
}