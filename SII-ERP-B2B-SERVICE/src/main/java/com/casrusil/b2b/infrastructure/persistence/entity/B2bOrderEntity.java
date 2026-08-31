package com.casrusil.b2b.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "b2b_orders")
@Data
public class B2bOrderEntity {
    @Id
    private UUID id;
    private String companyId;
    private String customerId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "b2b_order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<B2bOrderItemEntity> items = new ArrayList<>();
}
