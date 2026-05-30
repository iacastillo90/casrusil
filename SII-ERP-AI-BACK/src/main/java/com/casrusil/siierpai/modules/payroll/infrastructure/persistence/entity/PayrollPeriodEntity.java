package com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "pay_periods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollPeriodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyId;
    
    @Column(nullable = false)
    private YearMonth period;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal ufValue;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal utmValue;
    
    @Column(nullable = false)
    private boolean isClosed;
}
