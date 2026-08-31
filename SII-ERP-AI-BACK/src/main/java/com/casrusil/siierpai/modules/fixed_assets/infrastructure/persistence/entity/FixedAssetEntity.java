package com.casrusil.siierpai.modules.fixed_assets.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fixed_assets")
@Data
public class FixedAssetEntity {
    @Id
    private UUID id;
    private String companyId;
    private String name;
    private String category;
    private BigDecimal purchaseValue;
    private LocalDate purchaseDate;
    private BigDecimal currentResidualValue;
}