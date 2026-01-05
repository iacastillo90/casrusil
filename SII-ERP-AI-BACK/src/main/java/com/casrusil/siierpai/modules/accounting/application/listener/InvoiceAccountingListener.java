package com.casrusil.siierpai.modules.accounting.application.listener;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountConstants;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryBuilder;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Listener que clasifica inteligentemente las compras para generar el Asiento
 * Contable.
 * Aplica lógica de "Financial God Mode": Activo vs Gasto.
 * 
 * ✅ REFACTORIZADO: Ahora usa AccountConstants para evitar discrepancias.
 */
@Component
public class InvoiceAccountingListener {

    private final AccountingEntryService accountingEntryService;
    private final InvoiceRepository invoiceRepository;

    public InvoiceAccountingListener(AccountingEntryService accountingEntryService,
            InvoiceRepository invoiceRepository) {
        this.accountingEntryService = accountingEntryService;
        this.invoiceRepository = invoiceRepository;
    }

    @EventListener
    public void handle(InvoiceCreatedEvent event) {
        // Obtenemos la factura completa
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(event.invoice().getId());
        if (invoiceOpt.isEmpty())
            return;

        Invoice invoice = invoiceOpt.get();

        // Solo procesamos COMPRAS aquí para la lógica de Activo vs Gasto
        if (isPurchase(invoice)) {
            createPurchaseEntry(invoice);
        } else {
            createSaleEntry(invoice);
        }
    }

    private boolean isPurchase(Invoice invoice) {
        // Si el emisor NO es la empresa actual, es una compra (recibida)
        // O basarse en TransactionType si está seteado
        return invoice
                .getTransactionType() == com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.PURCHASE;
    }

    private void createPurchaseEntry(Invoice invoice) {
        AccountingEntryBuilder builder = new AccountingEntryBuilder();

        // 1. Haber: Proveedores (Total)
        builder.addLine(AccountConstants.PROVEEDORES_NACIONALES, "Proveedores Nacionales", BigDecimal.ZERO,
                invoice.getTotalAmount());

        // 2. Debe: IVA (Si aplica)
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            builder.addLine(AccountConstants.IVA_CREDITO, "IVA Crédito Fiscal", invoice.getTaxAmount(),
                    BigDecimal.ZERO);
        }

        // 3. Debe: Neto (Clasificado)
        String accountCode = determineAssetOrExpenseAccount(invoice);
        String accountName = getAccountName(accountCode);

        builder.addLine(accountCode, accountName, invoice.getNetAmount(), BigDecimal.ZERO);

        if (builder.isBalanced()) {
            AccountingEntry entry = new AccountingEntry(
                    invoice.getCompanyId(),
                    invoice.getDate(), // Fecha contable = Fecha Emisión
                    "Compra: " + invoice.getBusinessName(),
                    invoice.getFolio().toString(),
                    "INVOICE",
                    invoice.getIssuerRut(),
                    invoice.getBusinessName(),
                    String.valueOf(invoice.getType().getCode()),
                    String.valueOf(invoice.getFolio()),
                    "POSTED",
                    builder.build(),
                    EntryType.PURCHASE);
            accountingEntryService.recordEntry(entry);
        }
    }

    private void createSaleEntry(Invoice invoice) {
        AccountingEntryBuilder builder = new AccountingEntryBuilder();

        // 1. Debe: Clientes (Total)
        // ✅ CORRECCIÓN CRÍTICA: Cambiado de 1.1.02.001 a 1.1.05.001
        builder.addLine(AccountConstants.CLIENTES_NACIONALES, "Clientes Nacionales", invoice.getTotalAmount(),
                BigDecimal.ZERO);

        // 2. Haber: IVA (Si aplica)
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            builder.addLine(AccountConstants.IVA_DEBITO, "IVA Débito Fiscal", BigDecimal.ZERO, invoice.getTaxAmount());
        }

        // 3. Haber: Ingresos
        builder.addLine(AccountConstants.VENTAS_NACIONALES, "Ventas Nacionales", BigDecimal.ZERO,
                invoice.getNetAmount());

        if (builder.isBalanced()) {
            AccountingEntry entry = new AccountingEntry(
                    invoice.getCompanyId(),
                    invoice.getDate(),
                    "Venta: " + invoice.getBusinessName(),
                    invoice.getFolio().toString(),
                    "INVOICE",
                    invoice.getReceiverRut(),
                    invoice.getBusinessName(),
                    String.valueOf(invoice.getType().getCode()),
                    String.valueOf(invoice.getFolio()),
                    "POSTED",
                    builder.build(),
                    EntryType.SALE);
            accountingEntryService.recordEntry(entry);
        }
    }

    private String determineAssetOrExpenseAccount(Invoice invoice) {
        String glosa = (invoice.getBusinessName() + " " + invoice.getItemsSummary()).toUpperCase();

        // 1. Reglas de Activo Fijo
        if (glosa.contains("MAQUINARIA") || glosa.contains("EQUIPO") || glosa.contains("COMPUTADOR")
                || glosa.contains("VEHICULO") || glosa.contains("MUEBLE")) {
            return AccountConstants.ACTIVO_FIJO;
        }

        // 2. Reglas de Gastos Puros (Servicios)
        if (glosa.contains("ARRIENDO") || glosa.contains("LUZ") || glosa.contains("AGUA")
                || glosa.contains("INTERNET") || glosa.contains("ASESORIA") || glosa.contains("SERVICIO")
                || glosa.contains("COMISION") || glosa.contains("SEGURO")) {
            return AccountConstants.GASTOS_GENERALES;
        }

        // 3. Reglas de Inventario (Compras del Giro)
        if (glosa.contains("ALIMENTOS") || glosa.contains("BEBIDAS") || glosa.contains("DISTRIBUIDORA")
                || glosa.contains("CARNES") || glosa.contains("SUPERMERCADO") || glosa.contains("HARINA")
                || glosa.contains("ABARROTES")) {
            return AccountConstants.EXISTENCIAS;
        }

        // 4. Default: Existencias por Clasificar (Activo Transitorio)
        return AccountConstants.EXISTENCIAS_POR_CLASIFICAR;
    }

    private String getAccountName(String code) {
        return switch (code) {
            case AccountConstants.EXISTENCIAS -> "Mercaderías (Existencias)";
            case AccountConstants.GASTOS_GENERALES -> "Gastos Generales";
            case AccountConstants.ACTIVO_FIJO -> "Activo Fijo (PPE)";
            case AccountConstants.EXISTENCIAS_POR_CLASIFICAR -> "Existencias por Clasificar";
            default -> "Cuenta " + code;
        };
    }
}
