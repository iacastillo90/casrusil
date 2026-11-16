package com.casrusil.siierpai.modules.invoicing.application.service;

import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.invoicing.domain.dto.UpdateInvoiceCommand;
import com.casrusil.siierpai.modules.invoicing.domain.dto.UpdateInvoiceCommand.UpdateInvoiceLineCommand;
import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.exception.InvalidInvoiceException;
import com.casrusil.siierpai.modules.invoicing.domain.exception.InvoiceAlreadyExistsException;
import com.casrusil.siierpai.modules.invoicing.domain.exception.InvoiceNotFoundException;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceAuditLog;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.ManageInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.UpdateInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class InvoiceManagementService implements ManageInvoiceUseCase, CreateInvoiceUseCase, SearchInvoicesUseCase,
        UpdateInvoiceUseCase {

    private static final List<String> INVOICE_REFERENCE_TYPES = List.of("INVOICE", "FACTURA_AI");

    private final InvoiceRepository invoiceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final InvoiceAuditTrailService auditTrailService;
    private final AccountingEntryRepository accountingEntryRepository;

    public InvoiceManagementService(InvoiceRepository invoiceRepository,
            ApplicationEventPublisher eventPublisher,
            InvoiceAuditTrailService auditTrailService,
            AccountingEntryRepository accountingEntryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.eventPublisher = eventPublisher;
        this.auditTrailService = auditTrailService;
        this.accountingEntryRepository = accountingEntryRepository;
    }

    @Override
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        CompanyId companyId = CompanyContext.requireCompanyId();

        if (!invoice.getCompanyId().equals(companyId)) {
            throw new InvalidInvoiceException("Invoice company ID does not match current context");
        }

        if (invoiceRepository.existsByCompanyIdAndTypeCodeAndFolioAndIssuerRut(
                companyId, invoice.getType().getCode(), invoice.getFolio(), invoice.getIssuerRut())) {
            throw new InvoiceAlreadyExistsException("Invoice already exists: " + invoice.getFolio());
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        auditTrailService.record(companyId, savedInvoice.getId(), InvoiceAuditLog.ACTION_CREATED,
                "Invoice folio " + savedInvoice.getFolio() + " created");
        eventPublisher.publishEvent(new InvoiceCreatedEvent(savedInvoice));
        return savedInvoice;
    }

    @Override
    @Transactional
    public Invoice updateInvoice(UUID id, UpdateInvoiceCommand command) {
        CompanyId companyId = CompanyContext.requireCompanyId();

        Invoice existing = invoiceRepository.findById(id)
                .filter(invoice -> invoice.getCompanyId().equals(companyId))
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        BigDecimal netAmount = requireNonNegative(command.netAmount(), "netAmount");
        BigDecimal taxAmount = requireNonNegative(command.taxAmount(), "taxAmount");
        BigDecimal totalAmount = requirePositive(command.totalAmount(), "totalAmount");

        boolean amountsChanged = hasChanged(existing.getNetAmount(), netAmount)
                || hasChanged(existing.getTaxAmount(), taxAmount)
                || hasChanged(existing.getTotalAmount(), totalAmount);

        if (amountsChanged && hasAccountingEntries(companyId, existing.getFolio())) {
            throw new IllegalStateException(
                    "Invoice " + existing.getFolio() + " has accounting entries; amounts cannot be updated");
        }

        InvoiceType type = existing.getType() != null
                ? existing.getType()
                : resolveType(command.type() != null ? command.type() : "FACTURA");

        Invoice updated = new Invoice(
                existing.getId(),
                companyId,
                type,
                command.folio() != null ? command.folio() : existing.getFolio(),
                hasText(command.issuerRut()) ? command.issuerRut().trim() : existing.getIssuerRut(),
                hasText(command.clientRut()) ? command.clientRut().trim() : existing.getReceiverRut(),
                hasText(command.recipientName()) ? command.recipientName().trim() : existing.getBusinessName(),
                hasText(command.issueDate()) ? parseIssueDate(command.issueDate()) : existing.getDate(),
                existing.getDueDate(),
                netAmount,
                taxAmount,
                totalAmount,
                existing.getFixedAssetAmount(),
                existing.getCommonUseVatAmount(),
                existing.getOrigin(),
                existing.getTransactionType(),
                existing.getStatus(),
                buildLines(command.lines()),
                existing.getCurrency());

        Invoice savedInvoice = invoiceRepository.save(updated);
        auditTrailService.record(companyId, savedInvoice.getId(), InvoiceAuditLog.ACTION_UPDATED,
                "Invoice folio " + savedInvoice.getFolio() + " updated");
        return savedInvoice;
    }

    @Override
    public Invoice getInvoice(UUID id) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return invoiceRepository.findById(id)
                .filter(invoice -> invoice.getCompanyId().equals(companyId))
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    }

    @Override
    public List<Invoice> getInvoices() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return invoiceRepository.findByCompanyId(companyId);
    }

    @Override
    public List<Invoice> getInvoicesByCompany(CompanyId companyId) {
        return invoiceRepository.findByCompanyId(companyId);
    }

    private boolean hasAccountingEntries(CompanyId companyId, Long folio) {
        if (folio == null) {
            return false;
        }
        return !accountingEntryRepository
                .findByCompanyIdAndReferenceTypeInAndReferenceId(companyId, INVOICE_REFERENCE_TYPES,
                        String.valueOf(folio))
                .isEmpty();
    }

    private List<InvoiceLine> buildLines(List<UpdateInvoiceLineCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new InvalidInvoiceException("At least one invoice line is required");
        }

        List<InvoiceLine> lines = new ArrayList<>(commands.size());
        int lineNumber = 1;
        for (UpdateInvoiceLineCommand command : commands) {
            if (command == null) {
                throw new InvalidInvoiceException("Invoice lines cannot be null");
            }
            if (!hasText(command.description())) {
                throw new InvalidInvoiceException("Line description is required");
            }
            BigDecimal quantity = requirePositive(command.quantity(), "line quantity");
            BigDecimal unitPrice = requireNonNegative(command.unitPrice(), "line unitPrice");
            BigDecimal amount = command.lineTotal() != null
                    ? requireNonNegative(command.lineTotal(), "line lineTotal")
                    : quantity.multiply(unitPrice);
            lines.add(new InvoiceLine(lineNumber++, command.description().trim(), null, quantity, unitPrice, amount,
                    "UN"));
        }
        return lines;
    }

    private InvoiceType resolveType(String rawType) {
        String normalized = rawType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "FACTURA", "FACTURA_ELECTRONICA" -> InvoiceType.FACTURA_ELECTRONICA;
            case "BOLETA", "BOLETA_ELECTRONICA" -> InvoiceType.BOLETA_ELECTRONICA;
            default -> {
                try {
                    yield InvoiceType.fromCode(Integer.parseInt(normalized));
                } catch (IllegalArgumentException ex) {
                    throw new InvalidInvoiceException("Unsupported invoice type: " + rawType);
                }
            }
        };
    }

    private LocalDate parseIssueDate(String value) {
        String trimmed = value.trim();
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeException ignored) {
            try {
                return OffsetDateTime.parse(trimmed).toLocalDate();
            } catch (DateTimeException ex) {
                throw new InvalidInvoiceException("Invalid issueDate: " + value);
            }
        }
    }

    private BigDecimal requireNonNegative(BigDecimal value, String field) {
        if (value == null) {
            throw new InvalidInvoiceException(field + " is required");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInvoiceException(field + " cannot be negative");
        }
        return value;
    }

    private BigDecimal requirePositive(BigDecimal value, String field) {
        if (value == null) {
            throw new InvalidInvoiceException(field + " is required");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInvoiceException(field + " must be greater than zero");
        }
        return value;
    }

    private boolean hasChanged(BigDecimal current, BigDecimal updated) {
        if (current == null) {
            return updated != null;
        }
        return updated == null || current.compareTo(updated) != 0;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
