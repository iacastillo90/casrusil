package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builder para Asientos Contables con "Cero Error Matemático".
 * - Fusiona líneas de la misma cuenta.
 * - Garantiza precision en BigDecimal.
 * - Valida 'Partida Doble'.
 */
public class AccountingEntryBuilder {
    private final Map<String, AccountingEntryLine> lineMap = new HashMap<>();

    // Agrega o fusiona una línea
    public void addLine(String accountCode, String accountName, BigDecimal debit, BigDecimal credit) {
        // Normalizar nulos
        debit = (debit == null) ? BigDecimal.ZERO : debit;
        credit = (credit == null) ? BigDecimal.ZERO : credit;

        if (lineMap.containsKey(accountCode)) {
            // Fusión inteligente: Si ya existe la cuenta, sumamos los montos
            AccountingEntryLine existing = lineMap.get(accountCode);
            BigDecimal newDebit = existing.debit().add(debit);
            BigDecimal newCredit = existing.credit().add(credit);

            // Re-calcular saldo neto de la línea si se desea, o mantener bruto.
            // Aqui mantenemos bruto acumulado per cuenta.
            lineMap.put(accountCode, new AccountingEntryLine(accountCode, accountName, newDebit, newCredit));
        } else {
            lineMap.put(accountCode, new AccountingEntryLine(accountCode, accountName, debit, credit));
        }
    }

    public List<AccountingEntryLine> build() {
        return lineMap.values().stream()
                // Filtrar líneas en cero
                .filter(line -> line.debit().compareTo(BigDecimal.ZERO) > 0
                        || line.credit().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(AccountingEntryLine::accountCode))
                .collect(Collectors.toList());
    }

    // Validación de cuadratura
    public boolean isBalanced() {
        BigDecimal totalDebit = lineMap.values().stream().map(AccountingEntryLine::debit).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal totalCredit = lineMap.values().stream().map(AccountingEntryLine::credit).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        return totalDebit.compareTo(totalCredit) == 0;
    }
}
