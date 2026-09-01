package com.casrusil.siierpai.modules.procurement.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrder;
import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrderLine;
import com.casrusil.siierpai.modules.procurement.domain.port.in.PurchaseOrderUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/procurement/orders")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class PurchaseOrderController {

    private final PurchaseOrderUseCase purchaseOrderUseCase;

    public PurchaseOrderController(PurchaseOrderUseCase purchaseOrderUseCase) {
        this.purchaseOrderUseCase = purchaseOrderUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getPurchaseOrders() {
        CompanyId companyId = CompanyContext.getCompanyId();
        return ResponseEntity.ok(purchaseOrderUseCase.getPurchaseOrdersByCompany(companyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getPurchaseOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseOrderUseCase.getPurchaseOrder(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        
        List<PurchaseOrderLine> lines = request.lines().stream()
                .map(l -> new PurchaseOrderLine(UUID.randomUUID(), l.productId(), l.description(), l.quantity(), l.unitPrice()))
                .collect(Collectors.toList());

        PurchaseOrder po = purchaseOrderUseCase.createPurchaseOrder(
                companyId,
                request.supplierRut(),
                request.supplierName(),
                request.expectedDeliveryDate(),
                lines
        );

        return ResponseEntity.ok(po);
    }

    @PostMapping("/{id}/sent")
    public ResponseEntity<PurchaseOrder> markAsSent(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseOrderUseCase.markAsSent(id));
    }

    @PostMapping("/{id}/received")
    public ResponseEntity<PurchaseOrder> markAsReceived(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseOrderUseCase.markAsReceived(id));
    }

    public record CreatePurchaseOrderRequest(
            String supplierRut,
            String supplierName,
            LocalDate expectedDeliveryDate,
            List<CreatePurchaseOrderLineRequest> lines
    ) {}

    public record CreatePurchaseOrderLineRequest(
            UUID productId,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice
    ) {}
}
