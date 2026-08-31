package com.casrusil.siierpai.modules.dms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "corporate_documents")
@Data
public class CorporateDocumentEntity {
    @Id
    private UUID id;
    private String companyId;
    private String title;
    private String documentType;
    private String storageUrl;
    private LocalDate expirationDate;
    
    @Column(columnDefinition = "TEXT")
    private String aiSummary;
}