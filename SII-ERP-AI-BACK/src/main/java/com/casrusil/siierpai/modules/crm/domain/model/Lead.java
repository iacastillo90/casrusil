package com.casrusil.siierpai.modules.crm.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.LocalDateTime;
import java.util.UUID;

public class Lead {
    private UUID id;
    private CompanyId companyId;
    private String contactName;
    private String companyName;
    private String email;
    private String phone;
    private LeadStatus status;
    private LocalDateTime createdAt;

    public Lead(UUID id, CompanyId companyId, String contactName, String companyName, String email, String phone, LeadStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.contactName = contactName;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Lead create(CompanyId companyId, String contactName, String companyName, String email, String phone) {
        return new Lead(UUID.randomUUID(), companyId, contactName, companyName, email, phone, LeadStatus.NEW, LocalDateTime.now());
    }

    public void updateStatus(LeadStatus newStatus) {
        this.status = newStatus;
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getContactName() { return contactName; }
    public String getCompanyName() { return companyName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LeadStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
