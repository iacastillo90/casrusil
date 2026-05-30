package com.casrusil.siierpai.modules.period_close.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_insights")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AiInsightEntity {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String companyId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String analysisContext;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String generatedInsight;
    
    @Column(nullable = false)
    private LocalDateTime generatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String suggestedAction;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
