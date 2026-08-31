package com.casrusil.siierpai.modules.dms.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.time.LocalDate;
import java.util.UUID;

public class CorporateDocument {
    private final UUID id;
    private final CompanyId companyId;
    private final String title;
    private final String documentType; // CONTRACT, DEED, NDA
    private final String storageUrl;
    private LocalDate expirationDate;
    private String aiSummary;

    public CorporateDocument(UUID id, CompanyId companyId, String title, String documentType, String storageUrl, LocalDate expirationDate, String aiSummary) {
        this.id = id;
        this.companyId = companyId;
        this.title = title;
        this.documentType = documentType;
        this.storageUrl = storageUrl;
        this.expirationDate = expirationDate;
        this.aiSummary = aiSummary;
    }

    public static CorporateDocument create(CompanyId companyId, String title, String documentType, String storageUrl) {
        return new CorporateDocument(UUID.randomUUID(), companyId, title, documentType, storageUrl, null, null);
    }

    public void updateAiAnalysis(LocalDate expirationDate, String summary) {
        this.expirationDate = expirationDate;
        this.aiSummary = summary;
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getTitle() { return title; }
    public String getDocumentType() { return documentType; }
    public String getStorageUrl() { return storageUrl; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public String getAiSummary() { return aiSummary; }
}