package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.*;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpeningBalanceService {

    private final AccountingEntryService accountingEntryService;
    private final AccountRepository accountRepository;

    public OpeningBalanceService(AccountingEntryService accountingEntryService, AccountRepository accountRepository) {
        this.accountingEntryService = accountingEntryService;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void setOpeningBalance(CompanyId companyId, List<OpeningBalanceItem> items) {
        // 1. Crear cuentas faltantes
        for (OpeningBalanceItem item : items) {
            ensureAccountExists(companyId, item);
        }

        // 2. Construir Asiento con Builder (Fusión y Signos Correctos)
        com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryBuilder builder = new com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryBuilder();

        for (OpeningBalanceItem item : items) {
            String code = item.accountCode();
            // Normalizar montos
            BigDecimal net = item.debit().subtract(item.credit());
            BigDecimal amount = net.abs();

            char firstDigit = code.charAt(0);
            BigDecimal debit = BigDecimal.ZERO;
            BigDecimal credit = BigDecimal.ZERO;

            if (firstDigit == '1' || firstDigit == '5' || firstDigit == '6') {
                debit = amount;
            } else {
                credit = amount;
            }

            String name = (item.accountName() != null) ? item.accountName() : "Saldo Inicial";
            builder.addLine(code, name, debit, credit);
        }

        if (builder.isBalanced()) {
            AccountingEntry entry = new AccountingEntry(
                    companyId, java.time.LocalDate.now(), "Asiento de Apertura (Corregido)", "OPENING-BAL",
                    "OPENING", null, null, null, null, "POSTED", builder.build(), EntryType.OPENING);
            accountingEntryService.recordEntry(entry);
        } else {
            throw new IllegalArgumentException("El Asiento de Apertura no cuadra (" + builder.isBalanced() + ")");
        }
    }

    private void ensureAccountExists(CompanyId companyId, OpeningBalanceItem item) {
        if (accountRepository.findByCode(companyId, item.accountCode()).isEmpty()) {
            AccountType type = inferTypeFromCode(item.accountCode());
            String name = (item.accountName() != null && !item.accountName().isBlank())
                    ? item.accountName()
                    : "Cuenta Importada " + item.accountCode();

            Account newAccount = new Account(
                    companyId,
                    item.accountCode(),
                    name,
                    type,
                    "Auto-generada por Carga de Balance");
            accountRepository.save(newAccount);
        }
    }

    private AccountType inferTypeFromCode(String code) {
        if (code == null || code.isEmpty())
            return AccountType.ASSET;
        char firstDigit = code.charAt(0);
        return switch (firstDigit) {
            case '1' -> AccountType.ASSET;
            case '2' -> AccountType.LIABILITY;
            case '3' -> AccountType.EQUITY;
            case '4' -> AccountType.REVENUE;
            case '5' -> AccountType.EXPENSE;
            case '6' -> AccountType.EXPENSE;
            default -> AccountType.ASSET;
        };
    }

    public record OpeningBalanceItem(String accountCode, String accountName, BigDecimal debit, BigDecimal credit) {
    }
}
