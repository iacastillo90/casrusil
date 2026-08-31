package com.casrusil.siierpai.modules.wms.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wms_picking_tasks")
@Data
public class PickingTaskEntity {
    @Id
    private UUID id;
    private String companyId;
    private String orderId;
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wms_picking_items", joinColumns = @JoinColumn(name = "task_id"))
    private List<PickingItemEntity> items = new ArrayList<>();
}