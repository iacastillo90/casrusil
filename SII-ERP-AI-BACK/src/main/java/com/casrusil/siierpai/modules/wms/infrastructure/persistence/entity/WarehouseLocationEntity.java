package com.casrusil.siierpai.modules.wms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "wms_locations")
@Data
public class WarehouseLocationEntity {
    @Id
    private UUID id;
    private String companyId;
    private String code;
    private String description;
    private String zone;
}