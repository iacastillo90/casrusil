package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "sii_cafs", indexes = { @Index(name = "idx_caf_company", columnList = "company_id") })
@Data
public class CafEntity {
    @Id
    private UUID id;
    private String companyId;
    private String xmlContent;
    private Long initialFolio;
    private Long finalFolio;
    private Long currentFolio;
}