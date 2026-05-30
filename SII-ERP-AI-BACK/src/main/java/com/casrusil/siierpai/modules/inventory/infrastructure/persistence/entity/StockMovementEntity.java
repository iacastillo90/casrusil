package com.casrusil.siierpai.modules.inventory.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.inventory.domain.model.MovementType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inv_stock_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StockMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyId;
    
    @Column(nullable = false)
    private Long productId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalCost;
    
    @Column(nullable = false)
    private LocalDateTime movementDate;
    
    private String referenceDocument;
    
    private String description;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
