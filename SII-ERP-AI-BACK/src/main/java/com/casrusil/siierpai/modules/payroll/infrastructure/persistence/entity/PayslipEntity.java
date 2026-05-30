package com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pay_payslips")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayslipEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyId;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private Long periodId;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal baseSalary;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal gratification;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal taxableIncome;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal nonTaxableIncome;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal afpDiscount;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal healthDiscount;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal unemploymentInsurance;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal singleTax;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal totalDiscounts;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal netSalary;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
