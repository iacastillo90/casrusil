package com.casrusil.siierpai.modules.pos.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.pos.domain.model.PosTransaction;
import com.casrusil.siierpai.modules.pos.domain.model.PosTransactionItem;
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

    public PosController(PosUseCase posUseCase) {
        this.posUseCase = posUseCase;
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
