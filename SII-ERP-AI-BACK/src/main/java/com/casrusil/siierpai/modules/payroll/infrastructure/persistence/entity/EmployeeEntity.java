package com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pay_employees")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyId;
    
    @Column(nullable = false)
    private String rut;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal baseSalary;
    
    @Column(nullable = false)
    private String afp;
    
    @Column(nullable = false)
    private String healthSystem;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal healthPercentage;
    
    private Integer familyLoads;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
