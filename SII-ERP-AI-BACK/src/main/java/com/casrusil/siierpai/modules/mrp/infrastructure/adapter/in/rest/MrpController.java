package com.casrusil.siierpai.modules.mrp.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.mrp.application.service.MrpServiceImpl;
import com.casrusil.siierpai.modules.mrp.domain.model.BillOfMaterials;
import com.casrusil.siierpai.modules.mrp.domain.model.WorkOrder;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mrp")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class MrpController {

    private final MrpServiceImpl mrpService;

    public MrpController(MrpServiceImpl mrpService) {
        this.mrpService = mrpService;
    }

    @PostMapping("/bom")
    public ResponseEntity<BillOfMaterials> createBom(@Valid @RequestBody BomRequest req) {
        return ResponseEntity.ok(mrpService.createBom(CompanyContext.requireCompanyId(), req.productId(), req.name(), req.items()));
    }

    @GetMapping("/bom")
    public ResponseEntity<List<BillOfMaterials>> getBoms() {
        return ResponseEntity.ok(mrpService.getBoms(CompanyContext.requireCompanyId()));
    }

    @PostMapping("/work-order")
    public ResponseEntity<WorkOrder> createWorkOrder(@Valid @RequestBody WorkOrderRequest req) {
        return ResponseEntity.ok(mrpService.createWorkOrder(CompanyContext.requireCompanyId(), req.bomId(), req.targetQuantity()));
    }

    @GetMapping("/work-order")
    public ResponseEntity<List<WorkOrder>> getWorkOrders() {
        return ResponseEntity.ok(mrpService.getWorkOrders(CompanyContext.requireCompanyId()));
    }

    @PostMapping("/work-order/{id}/complete")
    public ResponseEntity<Void> completeWorkOrder(@PathVariable UUID id) {
        mrpService.completeWorkOrder(CompanyContext.requireCompanyId(), id);
        return ResponseEntity.ok().build();
    }

    public record BomRequest(String productId, String name, List<BillOfMaterials.BomItem> items) {}
    public record WorkOrderRequest(UUID bomId, BigDecimal targetQuantity) {}
}