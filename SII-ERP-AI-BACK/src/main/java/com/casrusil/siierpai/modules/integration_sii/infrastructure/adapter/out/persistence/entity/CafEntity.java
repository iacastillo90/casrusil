package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sii_cafs", indexes = { @Index(name = "idx_caf_company", columnList = "company_id") }, uniqueConstraints = @UniqueConstraint(name = "uk_caf_company_type_range", columnNames = {
        "company_id", "tipo_dte", "initial_folio", "final_folio" }))
@Data
public class CafEntity {
    @Id
    private UUID id;
    private String companyId;
    @Column(columnDefinition = "TEXT")
    private String xmlContent;
    private Long initialFolio;
    private Long finalFolio;
    private Long currentFolio;
    private String tipoDte;
    private LocalDate fechaAutorizacion;
    private LocalDate fechaVencimiento;
}
