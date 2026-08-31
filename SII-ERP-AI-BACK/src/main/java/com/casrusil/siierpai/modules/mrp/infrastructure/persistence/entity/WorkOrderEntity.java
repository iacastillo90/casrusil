package com.casrusil.siierpai.modules.mrp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mrp_work_orders")
@Data
public class WorkOrderEntity {
    @Id
    private UUID id;
    private String companyId;
    private UUID bomId;
    private BigDecimal targetQuantity;
    private String status;
    private LocalDate createdAt;
}