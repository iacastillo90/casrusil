package com.casrusil.siierpai.modules.financial_shield.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "fs_green_suppliers")
public class GreenSupplierEntity {

    @Id
    private UUID id;
    
    private String name;
    private String category;
    private String certificationLevel;
    
    public GreenSupplierEntity() {}
    
    public GreenSupplierEntity(UUID id, String name, String category, String certificationLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.certificationLevel = certificationLevel;
    }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getCertificationLevel() { return certificationLevel; }
    public void setCertificationLevel(String certificationLevel) { this.certificationLevel = certificationLevel; }
}
