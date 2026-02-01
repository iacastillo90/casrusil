package com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.annual_close.domain.model.TaxRegime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CompanyTaxProfileEntity {
    @Id
    private String companyId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaxRegime regime;
    
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal ppmRate;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
