package com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccountingEntryApiDto(
        String id,
        LocalDate entryDate,
        String description,
        List<Line> lines) {

    public static AccountingEntryApiDto from(AccountingEntry entry) {
        return new AccountingEntryApiDto(
                entry.getId().toString(),
                entry.getEntryDate(),
                entry.getDescription(),
                entry.getLines().stream()
                        .map(line -> new Line(line.accountCode(), line.accountName(), line.debit(), line.credit()))
                        .toList());
    }

    public record Line(
            String accountCode,
            String accountName,
            BigDecimal debit,
            BigDecimal credit) {
    }
}
