package com.casrusil.siierpai.modules.accounting.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountingEntryTest {

    @Test
    void shouldCreateBalancedEntry() {
        CompanyId companyId = CompanyId.random();
        AccountingEntryLine debit = AccountingEntryLine.debit("110501", "Caja", new BigDecimal("1190"));
        AccountingEntryLine credit1 = AccountingEntryLine.credit("310101", "Ventas", new BigDecimal("1000"));
        AccountingEntryLine credit2 = AccountingEntryLine.credit("210401", "IVA", new BigDecimal("190"));

        AccountingEntry entry = new AccountingEntry(
               companyId,
                LocalDate.now(),
                "Test Entry",
                UUID.randomUUID().toString(),
                "INVOICE",
                "76123456-7",
                "Test Company",
                "33",
                "123",
                "DRAFT",
                List.of(debit, credit1, credit2),
                EntryType.NORMAL);

        assertNotNull(entry);
        assertEquals(3, entry.getLines().size());
    }

    @Test
    void shouldThrowExceptionWhenEntryIsUnbalanced() {
        CompanyId companyId = CompanyId.random();
        AccountingEntryLine debit = AccountingEntryLine.debit("110501", "Caja", new BigDecimal("1000"));
        AccountingEntryLine credit = AccountingEntryLine.credit("310101", "Ventas", new BigDecimal("900"));

        assertThrows(IllegalArgumentException.class, () -> {
            new AccountingEntry(
                   companyId,
                    LocalDate.now(),
                    "Unbalanced Entry",
                    UUID.randomUUID().toString(),
                    "INVOICE",
                    "76123456-7",
                    "Test Company",
                    "33",
                    "123",
                    "DRAFT",
                    List.of(debit, credit),
                    EntryType.NORMAL);
        });
    }

    @Test
    void shouldThrowExceptionWhenDebitOrCreditIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            AccountingEntryLine.debit("110501", "Caja", new BigDecimal("-100"));
        });
    }
}
