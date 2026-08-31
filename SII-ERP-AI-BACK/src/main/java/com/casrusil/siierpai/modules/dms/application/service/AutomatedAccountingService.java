package com.casrusil.siierpai.modules.dms.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AutomatedAccountingService {

    private final AccountingEntryRepository accountingEntryRepository;

    public AutomatedAccountingService(AccountingEntryRepository accountingEntryRepository) {
        this.accountingEntryRepository = accountingEntryRepository;
    }

    @Transactional
    public void createDraftFromInvoice(CompanyId companyId, InvoiceAiReaderService.ExtractedInvoiceData data) {

        // Asiento de Compras (Partida Doble):
        // Débito Gasto (510101) por el Neto
        // Débito IVA Crédito (110801) por el IVA
        // Crédito Proveedores (210101) por el Total
        List<AccountingEntryLine> lines = List.of(
            new AccountingEntryLine("510101", "Gasto Compras",
                    new BigDecimal(data.montoNeto()), BigDecimal.ZERO),
            new AccountingEntryLine("110801", "IVA Crédito Fiscal",
                    new BigDecimal(data.montoIva()), BigDecimal.ZERO),
            new AccountingEntryLine("210101", "Proveedores por Pagar",
                    BigDecimal.ZERO, new BigDecimal(data.montoTotal()))
        );

        // Use the 12-arg constructor with entryDate
        AccountingEntry entry = new AccountingEntry(
                companyId,
                data.fechaEmision(),
                "COMPRA AUTOMÁTICA AI - FAC #" + data.folio() + " - " + data.razonSocial(),
                String.valueOf(data.folio()),   // referenceId
                "FACTURA_AI",                   // referenceType
                data.rutProveedor(),             // taxPayerId
                data.razonSocial(),              // taxPayerName
                "33",                            // documentType (Factura Electrónica)
                String.valueOf(data.folio()),   // documentNumber
                "DRAFT",                         // status
                lines,
                EntryType.PURCHASE
        );

        accountingEntryRepository.save(entry);
    }
}