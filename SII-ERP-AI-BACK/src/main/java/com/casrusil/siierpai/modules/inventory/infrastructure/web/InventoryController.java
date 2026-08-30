package com.casrusil.siierpai.modules.inventory.infrastructure.web;

import com.casrusil.siierpai.modules.inventory.application.service.InventoryUseCase;
import com.casrusil.siierpai.modules.inventory.domain.dto.ProductRequest;
import com.casrusil.siierpai.modules.inventory.domain.dto.ProductResponse;
import com.casrusil.siierpai.modules.inventory.domain.dto.StockMovementRequest;
import com.casrusil.siierpai.modules.inventory.domain.dto.StockMovementResponse;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del módulo de Inventario.
 *
 * <p>SEGURIDAD: El {@code companyId} se extrae del {@link CompanyContext}, que es
 * inicializado por el {@code SecurityFilter} a partir del token JWT. Nunca se acepta
 * desde el request del cliente para prevenir el acceso cruzado entre empresas (tenants).
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryUseCase inventoryUseCase;

    /**
     * Obtiene el companyId del contexto JWT, no del request del cliente.
     * Esto garantiza el aislamiento de datos entre empresas (multi-tenancy).
     */
    private String getCompanyId() {
        return CompanyContext.requireCompanyId().getValue().toString();
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryUseCase.createProduct(request, getCompanyId()));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        return ResponseEntity.ok(inventoryUseCase.updateProduct(id, request, getCompanyId()));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        inventoryUseCase.deleteProduct(id, getCompanyId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryUseCase.getProduct(id, getCompanyId()));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(inventoryUseCase.getAllProducts(getCompanyId()));
    }

    @PostMapping("/products/{productId}/movements")
    public ResponseEntity<StockMovementResponse> registerMovement(
            @PathVariable Long productId,
            @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryUseCase.registerMovement(productId, request, getCompanyId()));
    }

    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<List<StockMovementResponse>> getProductMovements(
            @PathVariable Long productId) {
        return ResponseEntity.ok(inventoryUseCase.getProductMovements(productId, getCompanyId()));
    }
}
