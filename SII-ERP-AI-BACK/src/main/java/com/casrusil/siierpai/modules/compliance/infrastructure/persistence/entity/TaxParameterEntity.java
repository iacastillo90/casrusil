package com.casrusil.siierpai.modules.compliance.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "tax_parameters")
@Data
public class TaxParameterEntity {
    @Id
    private String id; // e.g., "YEAR_2024"
    private BigDecimal utmValue;
    private BigDecimal ufValue;
    private BigDecimal bheRetentionRate; // e.g., 13.75
}