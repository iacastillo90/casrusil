package com.casrusil.siierpai.modules.pos.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.pos.domain.model.PosTransaction;
import com.casrusil.siierpai.modules.pos.domain.model.PosTransactionItem;
import com.casrusil.siierpai.modules.inventory.domain.model.Product;
import com.casrusil.siierpai.modules.inventory.domain.port.ProductRepositoryPort;
import com.casrusil.siierpai.modules.pos.domain.port.in.PosUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pos")
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
public class PosController {

    private final PosUseCase posUseCase;
    private final ProductRepositoryPort productRepository;

    public PosController(PosUseCase posUseCase, ProductRepositoryPort productRepository) {
        this.posUseCase = posUseCase;
        this.productRepository = productRepository;
    }

    /**
     * Busca un producto del inventario por código de barras/SKU o nombre.
     *
     * @param query SKU exacto o parte del nombre
     * @return ítem listo para agregar al carro del POS
     */
    @GetMapping("/products/search")
    public ResponseEntity<ProductSearchResponse> searchProduct(@RequestParam String query) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        List<Product> products = productRepository.findAllByCompanyId(companyId.value().toString());

        return products.stream()
                .filter(p -> query.equalsIgnoreCase(p.getSku())
                        || (p.getName() != null && p.getName().toLowerCase().contains(query.toLowerCase())))
                .findFirst()
                .map(p -> ResponseEntity.ok(ProductSearchResponse.from(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record ProductSearchResponse(String productId, String productName, BigDecimal quantity,
            BigDecimal unitPrice, BigDecimal subtotal) {

        static ProductSearchResponse from(Product product) {
            BigDecimal price = product.getSalePrice() != null ? product.getSalePrice() : BigDecimal.ZERO;
            return new ProductSearchResponse(
                    String.valueOf(product.getId()),
                    product.getName(),
                    BigDecimal.ONE,
                    price,
                    price);
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<PosTransaction>> getTransactions() {
        CompanyId companyId = CompanyContext.getCompanyId();
        return ResponseEntity.ok(posUseCase.getTransactionsByCompany(companyId));
    }

    @PostMapping("/transactions")
    public ResponseEntity<PosTransaction> registerSale(@Valid @RequestBody RegisterSaleRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        
        List<PosTransactionItem> items = request.items().stream()
                .map(i -> new PosTransactionItem(UUID.randomUUID(), i.productId(), i.productName(), i.quantity(), i.unitPrice()))
                .collect(Collectors.toList());

        PosTransaction transaction = posUseCase.registerSale(companyId, request.posTerminalId(), request.cashierName(), request.paymentMethod(), items);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncPendingTransactions() {
        CompanyId companyId = CompanyContext.getCompanyId();
        posUseCase.syncPendingTransactions(companyId);
        return ResponseEntity.ok().build();
    }

    public record RegisterSaleRequest(
            String posTerminalId,
            String cashierName,
            String paymentMethod,
            List<RegisterSaleItemRequest> items
    ) {}

    public record RegisterSaleItemRequest(
            String productId,
            String productName,
            BigDecimal quantity,
            BigDecimal unitPrice
    ) {}
}
