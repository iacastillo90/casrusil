package com.casrusil.siierpai.modules.projects.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Project {
    private UUID id;
    private CompanyId companyId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, COMPLETED, ON_HOLD
    private BigDecimal budget;

    public Project(UUID id, CompanyId companyId, String name, String description, LocalDate startDate, LocalDate endDate, String status, BigDecimal budget) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.budget = budget;
    }

    public static Project create(CompanyId companyId, String name, String description, BigDecimal budget) {
        return new Project(UUID.randomUUID(), companyId, name, description, LocalDate.now(), null, "ACTIVE", budget);
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public BigDecimal getBudget() { return budget; }

    public void setStatus(String status) { this.status = status; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
