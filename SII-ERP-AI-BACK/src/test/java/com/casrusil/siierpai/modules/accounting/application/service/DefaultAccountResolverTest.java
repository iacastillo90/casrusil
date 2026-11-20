package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAccountResolverTest {

    private final StubAccountRepository repository = new StubAccountRepository();
    private final DefaultAccountResolver resolver = new DefaultAccountResolver(
            repository,
            "1.1.01.001",
            "4.1.01.001",
            "4201",
            "5.1.03.025",
            "5.1.03.025",
            "5.9.99.999",
            "5.9.99.999",
            "1.1.03.999");

    @Test
    void resolve_prefersCanonicalPlanWhenPresent() {
        CompanyId companyId = CompanyId.random();
        repository.accounts.add(account(companyId, "1.1.01.001", "Caja", AccountType.ASSET));
        repository.accounts.add(account(companyId, "4.1.01.001", "Ventas Nacionales", AccountType.REVENUE));
        repository.accounts.add(account(companyId, "110101", "Caja Moneda Nacional", AccountType.ASSET));
        repository.accounts.add(account(companyId, "410101", "Ventas Nacionales", AccountType.REVENUE));

        assertEquals("1.1.01.001", resolver.resolve(companyId, DefaultAccountRole.CASH));
        assertEquals("4.1.01.001", resolver.resolve(companyId, DefaultAccountRole.SALES));
    }

    @Test
    void resolve_fallsBackToDetailedPlanForNewCompany() {
        CompanyId companyId = CompanyId.random();
        repository.accounts.add(account(companyId, "110101", "Caja Moneda Nacional", AccountType.ASSET));
        repository.accounts.add(account(companyId, "410101", "Ventas Nacionales", AccountType.REVENUE));
        repository.accounts.add(account(companyId, "4201", "Otros Ingresos", AccountType.REVENUE));
        repository.accounts.add(account(companyId, "5204", "Servicios Básicos", AccountType.EXPENSE));

        assertEquals("110101", resolver.resolve(companyId, DefaultAccountRole.CASH));
        assertEquals("410101", resolver.resolve(companyId, DefaultAccountRole.SALES));
        assertEquals("4201", resolver.resolve(companyId, DefaultAccountRole.OTHER_INCOME));
        assertEquals("5204", resolver.resolve(companyId, DefaultAccountRole.OTHER_EXPENSE));
    }

    @Test
    void resolveCategory_mapsCategoriesToPlanAccounts() {
        CompanyId companyId = CompanyId.random();
        repository.accounts.add(account(companyId, "110101", "Caja Moneda Nacional", AccountType.ASSET));
        repository.accounts.add(account(companyId, "410101", "Ventas Nacionales", AccountType.REVENUE));
        repository.accounts.add(account(companyId, "4201", "Otros Ingresos", AccountType.REVENUE));
        repository.accounts.add(account(companyId, "5204", "Servicios Básicos", AccountType.EXPENSE));
        repository.accounts.add(account(companyId, "5202", "Honorarios", AccountType.EXPENSE));

        assertEquals("410101", resolver.resolveCategory(companyId, "SALES", false));
        assertEquals("5204", resolver.resolveCategory(companyId, "OFFICE_SUPPLIES", true));
        assertEquals("5204", resolver.resolveCategory(companyId, "SERVICES", true));
        assertEquals("4201", resolver.resolveCategory(companyId, "OTHER", false));
        assertEquals("5204", resolver.resolveCategory(companyId, "OTHER", true));
    }

    @Test
    void resolve_fallsBackToTypeAndNameWhenConfiguredAndCandidatesAreMissing() {
        CompanyId companyId = CompanyId.random();
        repository.accounts.add(account(companyId, "110201", "Banco Cuenta Corriente", AccountType.ASSET));
        repository.accounts.add(account(companyId, "5202", "Honorarios", AccountType.EXPENSE));

        assertEquals("110201", resolver.resolve(companyId, DefaultAccountRole.CASH));
        assertEquals("5202", resolver.resolve(companyId, DefaultAccountRole.OTHER_EXPENSE));
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
