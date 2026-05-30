package com.casrusil.siierpai.modules.inventory.infrastructure.web;

import com.casrusil.siierpai.modules.inventory.application.service.InventoryUseCase;
import com.casrusil.siierpai.modules.inventory.domain.dto.ProductRequest;
import com.casrusil.siierpai.modules.inventory.domain.dto.ProductResponse;
import com.casrusil.siierpai.modules.inventory.domain.dto.StockMovementRequest;
import com.casrusil.siierpai.modules.inventory.domain.dto.StockMovementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryUseCase inventoryUseCase;
    
    // TODO: In a real environment, companyId should be extracted from the JWT token in SecurityContext
    // For this implementation, we will pass it as a header for testing purposes
    private static final String COMPANY_HEADER = "X-Company-Id";

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryUseCase.createProduct(request, companyId));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        return ResponseEntity.ok(inventoryUseCase.updateProduct(id, request, companyId));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @PathVariable Long id) {
        inventoryUseCase.deleteProduct(id, companyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryUseCase.getProduct(id, companyId));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestHeader(COMPANY_HEADER) String companyId) {
        return ResponseEntity.ok(inventoryUseCase.getAllProducts(companyId));
    }

    @PostMapping("/products/{productId}/movements")
    public ResponseEntity<StockMovementResponse> registerMovement(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @PathVariable Long productId,
            @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryUseCase.registerMovement(productId, request, companyId));
    }

    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<List<StockMovementResponse>> getProductMovements(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(inventoryUseCase.getProductMovements(productId, companyId));
    }
}
