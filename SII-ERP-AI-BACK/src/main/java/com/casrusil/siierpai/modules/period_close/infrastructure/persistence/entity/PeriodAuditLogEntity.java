package com.casrusil.siierpai.modules.period_close.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Entity
@Table(name = "ai_period_audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PeriodAuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyId;
    
    @Column(nullable = false)
    private YearMonth period;
    
    @Column(nullable = false)
    private LocalDateTime auditDate;
    
    @ElementCollection
    @CollectionTable(name = "ai_period_audit_issues", joinColumns = @JoinColumn(name = "audit_log_id"))
    @Column(name = "issue_description")
    private List<String> issuesFound;
    
    @Column(nullable = false)
    private boolean isResolved;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
