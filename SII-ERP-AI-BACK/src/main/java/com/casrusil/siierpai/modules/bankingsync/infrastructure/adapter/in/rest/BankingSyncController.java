package com.casrusil.siierpai.modules.bankingsync.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.bankingsync.domain.model.BankConnection;
import com.casrusil.siierpai.modules.bankingsync.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.bankingsync.domain.port.in.BankingSyncUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/banking")
public class BankingSyncController {

    private final BankingSyncUseCase useCase;

    public BankingSyncController(BankingSyncUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/connections")
    public ResponseEntity<List<BankConnection>> getConnections() {
        CompanyId companyId = CompanyContext.getCompanyId();
        return ResponseEntity.ok(useCase.getConnections(companyId));
    }

    @PostMapping("/connections")
    public ResponseEntity<BankConnection> connectBank(@Valid @RequestBody ConnectBankRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        return ResponseEntity.ok(useCase.connectBank(companyId, request.bankName(), request.accountNumber()));
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncTransactions() {
        CompanyId companyId = CompanyContext.getCompanyId();
        useCase.syncTransactions(companyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<BankTransaction>> getTransactions() {
        CompanyId companyId = CompanyContext.getCompanyId();
        return ResponseEntity.ok(useCase.getTransactions(companyId));
    }

    @PostMapping("/transactions/{id}/reconcile")
    public ResponseEntity<Void> reconcileTransaction(@PathVariable UUID id) {
        useCase.reconcileTransaction(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transactions/{id}/ignore")
    public ResponseEntity<Void> ignoreTransaction(@PathVariable UUID id) {
        useCase.ignoreTransaction(id);
        return ResponseEntity.ok().build();
    }

    public record ConnectBankRequest(String bankName, String accountNumber) {}
}
