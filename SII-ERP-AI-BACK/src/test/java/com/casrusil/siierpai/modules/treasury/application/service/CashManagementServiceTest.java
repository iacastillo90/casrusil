package com.casrusil.siierpai.modules.treasury.application.service;

import com.casrusil.siierpai.modules.accounting.application.service.DefaultAccountResolver;
import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.modules.treasury.domain.model.CashTransaction;
import com.casrusil.siierpai.modules.treasury.infrastructure.persistence.repository.CashTransactionJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CashManagementServiceTest {

    private final StubAccountRepository accountRepository = new StubAccountRepository();
    private final DefaultAccountResolver resolver = new DefaultAccountResolver(
            accountRepository,
            "1.1.01.001",
            "4.1.01.001",
            "4201",
            "5.1.03.025",
            "5.1.03.025",
            "5.9.99.999",
            "5.9.99.999",
            "1.1.03.999");
    private final AccountingEntryService accountingEntryService = mock(AccountingEntryService.class);
    private final CashTransactionJpaRepository cashTransactionRepository = mock(CashTransactionJpaRepository.class);
    private final CashManagementService service = new CashManagementService(
            accountingEntryService,
            cashTransactionRepository,
            resolver);

    @Test
    void recordCashTransaction_usesResolvedAccountsForIncome() {
        CompanyId companyId = CompanyId.random();
        seedDetailedPlan(companyId);

        CashTransaction transaction = CashTransaction.create(
                companyId,
                LocalDate.of(2026, 11, 20),
                "Ingreso caja",
                new BigDecimal("1000"),
                null,
                "OTHER",
                "CLP",
                BigDecimal.ONE);

        service.recordCashTransaction(transaction);

        AccountingEntry entry = captureEntry();
        assertEquals(List.of("110101", "4201"), codes(entry.getLines()));
    }

    @Test
    void recordCashTransaction_usesResolvedAccountsForExpense() {
        CompanyId companyId = CompanyId.random();
        seedDetailedPlan(companyId);

        CashTransaction transaction = CashTransaction.create(
                companyId,
                LocalDate.of(2026, 11, 20),
                "Egreso caja",
                new BigDecimal("-1000"),
                null,
                "OTHER",
                "CLP",
                BigDecimal.ONE);

        service.recordCashTransaction(transaction);

        AccountingEntry entry = captureEntry();
        assertEquals(List.of("5204", "110101"), codes(entry.getLines()));
    }

    private AccountingEntry captureEntry() {
        ArgumentCaptor<AccountingEntry> captor = ArgumentCaptor.forClass(AccountingEntry.class);
        verify(accountingEntryService).recordEntry(captor.capture());
        return captor.getValue();
    }

    private static List<String> codes(List<AccountingEntryLine> lines) {
        return lines.stream().map(AccountingEntryLine::accountCode).toList();
    }

    private void seedDetailedPlan(CompanyId companyId) {
        accountRepository.accounts.add(account(companyId, "110101", "Caja Moneda Nacional", AccountType.ASSET));
        accountRepository.accounts.add(account(companyId, "4201", "Otros Ingresos", AccountType.REVENUE));
        accountRepository.accounts.add(account(companyId, "5204", "Servicios Básicos", AccountType.EXPENSE));
    }

    private static Account account(CompanyId companyId, String code, String name, AccountType type) {
        return new Account(UUID.randomUUID(), companyId, code, name, type, "test", true);
    }

    static class StubAccountRepository implements AccountRepository {
        final List<Account> accounts = new ArrayList<>();

        @Override
        public Account save(Account account) {
            accounts.add(account);
            return account;
        }

        @Override
        public Account saveAndFlush(Account account) {
            accounts.add(account);
            return account;
        }

        @Override
        public Optional<Account> findByCode(CompanyId companyId, String code) {
            return accounts.stream()
                    .filter(account -> account.getCompanyId().getValue().equals(companyId.getValue()))
                    .filter(account -> account.getCode().equals(code))
                    .findFirst();
        }

        @Override
        public List<Account> findAll(CompanyId companyId) {
            return accounts.stream()
                    .filter(account -> account.getCompanyId().getValue().equals(companyId.getValue()))
                    .toList();
        }
    }
}
