package com.casrusil.siierpai.modules.banking.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_learning_records")
@Data
public class ReconciliationLearningEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String companyId;

    @Column(nullable = false)
    private UUID bankTransactionId;

    @Column(nullable = false)
    private UUID accountingEntryId;

    @Column(nullable = false)
    private double suggestedConfidence;

    @Column(nullable = false)
    private boolean userConfirmed;

    private String userFeedback;

    @Column(nullable = false)
    private Instant createdAt;
}
