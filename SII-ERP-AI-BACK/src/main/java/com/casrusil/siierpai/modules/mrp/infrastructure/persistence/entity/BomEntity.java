package com.casrusil.siierpai.modules.mrp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mrp_boms")
@Data
@EntityListeners(AuditingEntityListener.class)
public class BomEntity {
    @Id
    private UUID id;
    private String companyId;
    private String productId;
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mrp_bom_items", joinColumns = @JoinColumn(name = "bom_id"))
    private List<BomItemEntity> items = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}