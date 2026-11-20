package com.casrusil.siierpai.modules.treasury.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.modules.treasury.domain.model.CashTransaction;
import com.casrusil.siierpai.modules.treasury.infrastructure.persistence.entity.CashTransactionEntity;
import com.casrusil.siierpai.modules.treasury.infrastructure.persistence.repository.CashTransactionJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CashManagementService {

    private final AccountingEntryService accountingEntryService;
    private final CashTransactionJpaRepository cashTransactionRepository;

    public CashManagementService(AccountingEntryService accountingEntryService,
            CashTransactionJpaRepository cashTransactionRepository) {
        this.accountingEntryService = accountingEntryService;
        this.cashTransactionRepository = cashTransactionRepository;
    }

    /**
     * Movimientos de caja de una empresa, más recientes primero.
     *
     * @param companyId empresa
     * @return movimientos registrados
     */
    public List<CashTransaction> listCashTransactions(CompanyId companyId) {
        return cashTransactionRepository.findByCompanyIdOrderByDateDesc(companyId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    private CashTransaction toDomain(CashTransactionEntity entity) {
        return CashTransaction.create(
                CompanyId.fromString(entity.getCompanyId().toString()),
                entity.getDate(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getReference(),
                entity.getCategory(),
                entity.getCurrency(),
                entity.getExchangeRate());
    }

    @Transactional
    public void recordCashTransaction(CashTransaction transaction) {
        // 1. Persistir el movimiento de caja.
        cashTransactionRepository.save(new CashTransactionEntity(
                transaction.getId(),
                transaction.getCompanyId().value(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getReference(),
                transaction.getCategory(),
                transaction.getCurrency(),
                transaction.getExchangeRate()));

        // 2. Create Accounting Entry
        createAccountingEntry(transaction);
    }

    private void createAccountingEntry(CashTransaction transaction) {
        List<AccountingEntryLine> lines = new ArrayList<>();
        String cashAccount = "110101"; // Caja

        BigDecimal convertedAmount = transaction.getAmount().multiply(transaction.getExchangeRate());

        if (convertedAmount.compareTo(BigDecimal.ZERO) > 0) {
            // Cash IN (Debit Cash, Credit Category/Revenue)
            lines.add(AccountingEntryLine.debit(cashAccount, "Cash Deposit (" + transaction.getCurrency() + ")", convertedAmount));
            // Determine credit account based on category
            String creditAccount = determineAccountFromCategory(transaction.getCategory(), false);
            lines.add(AccountingEntryLine.credit(creditAccount, "Revenue/Category", convertedAmount));
        } else {
            // Cash OUT (Debit Category/Expense, Credit Cash)
            BigDecimal absAmount = convertedAmount.abs();
            String debitAccount = determineAccountFromCategory(transaction.getCategory(), true);
            lines.add(AccountingEntryLine.debit(debitAccount, "Expense/Category", absAmount));
            lines.add(AccountingEntryLine.credit(cashAccount, "Cash Withdrawal (" + transaction.getCurrency() + ")", absAmount));
        }

        AccountingEntry entry = new AccountingEntry(
                transaction.getCompanyId(),
                transaction.getDescription(),
                transaction.getId().toString(),
                "CASH",
                null,
                null,
                null,
                null,
                "POSTED",
                lines,
                EntryType.NORMAL);

        accountingEntryService.recordEntry(entry);
    }

    private String determineAccountFromCategory(String category, boolean isExpense) {
        // Simple mapping logic
        if ("SALES".equalsIgnoreCase(category))
            return "310101"; // Ventas
        if ("OFFICE_SUPPLIES".equalsIgnoreCase(category))
            return "510102"; // Gastos de Oficina
        if ("SERVICES".equalsIgnoreCase(category))
            return "510103"; // Servicios
        return isExpense ? "510199" : "310199"; // Other Expense / Other Revenue
    }
}
