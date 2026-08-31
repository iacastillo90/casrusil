package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sii_tokens", indexes = { @Index(name = "idx_sii_token_company", columnList = "company_id") })
@Data
public class SiiTokenEntity {
    @Id
    private UUID id;
    private String companyId;
    private String token;
    private LocalDateTime expiration;
}