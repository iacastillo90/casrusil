package com.casrusil.siierpai.modules.invoicing.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.barcode.Pdf417Generator;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.TedGenerator;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceAuditLog;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.modules.invoicing.application.service.InvoiceAuditTrailService;
import com.casrusil.siierpai.modules.invoicing.infrastructure.mail.InvoiceEmailSender;
import com.casrusil.siierpai.modules.invoicing.infrastructure.pdf.InvoicePdfGenerator;
import com.casrusil.siierpai.modules.partners.domain.model.Partner;
import com.casrusil.siierpai.modules.partners.domain.port.out.PartnerRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

import java.util.UUID;

@Service
public class InvoiceDispatchService {

    private final InvoiceRepository invoiceRepository;
    private final CafRepository cafRepository;
    private final TedGenerator tedGenerator;
    private final Pdf417Generator pdf417Generator;
    private final InvoicePdfGenerator invoicePdfGenerator;
    private final InvoiceEmailSender invoiceEmailSender;
    private final InvoiceAuditTrailService auditTrailService;
    private final PartnerRepository partnerRepository;

    public InvoiceDispatchService(
            InvoiceRepository invoiceRepository,
            CafRepository cafRepository,
            TedGenerator tedGenerator,
            Pdf417Generator pdf417Generator,
            InvoicePdfGenerator invoicePdfGenerator,
            InvoiceEmailSender invoiceEmailSender,
            InvoiceAuditTrailService auditTrailService,
            PartnerRepository partnerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.cafRepository = cafRepository;
        this.tedGenerator = tedGenerator;
        this.pdf417Generator = pdf417Generator;
        this.invoicePdfGenerator = invoicePdfGenerator;
        this.invoiceEmailSender = invoiceEmailSender;
        this.auditTrailService = auditTrailService;
        this.partnerRepository = partnerRepository;
    }

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public void dispatchInvoice(UUID invoiceId, String recipientEmail) {
        // 1. Fetch Invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        // 2. La factura debe pertenecer a la empresa del usuario autenticado.
        CompanyId companyId = CompanyContext.requireCompanyId();
        if (!invoice.getCompanyId().equals(companyId)) {
            throw new AccessDeniedException("Invoice " + invoiceId + " does not belong to the current company");
        }

        // 3. Destinatario: si el receptor está en el maestro de partners y tiene
        // email registrado, se usa ese y no se permite enviar a otro. Si el
        // maestro no lo tiene, se acepta un email explícito validado.
        String requested = recipientEmail != null ? recipientEmail.trim() : "";
        String registered = partnerRepository
                .findByCompanyIdAndRut(companyId, invoice.getReceiverRut())
                .map(Partner::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .orElse(null);

        String recipient = registered != null ? registered.trim() : requested;
        if (registered != null && !requested.isEmpty() && !requested.equalsIgnoreCase(registered)) {
            throw new IllegalArgumentException(
                    "El destinatario debe ser el email registrado del receptor: " + registered);
        }
        if (!EMAIL.matcher(recipient).matches()) {
            throw new IllegalArgumentException("Invalid recipient email: " + recipientEmail);
        }

        // 4. Get CAF
        String tipoDteStr = String.valueOf(invoice.getType().getCode());
        Caf caf = cafRepository.findActiveForFolio(invoice.getCompanyId(), tipoDteStr, invoice.getFolio())
                .orElseThrow(() -> new IllegalStateException("No active CAF found for invoice " + invoice.getFolio()));

        // 5. Generate TED XML
        String tedXml = tedGenerator.generateTedXml(invoice, caf);

        // 6. Generate TED Image (PDF417)
        byte[] tedImage = pdf417Generator.generatePdf417(tedXml);

        // 7. Generate PDF
        byte[] pdfContent = invoicePdfGenerator.generatePdf(invoice, tedImage);

        // 8. Send Email
        String subject = "Documento Tributario Electrónico N° " + invoice.getFolio();
        String body = "Estimado cliente,\n\nAdjunto encontrará su Factura Electrónica N° " + invoice.getFolio()
                + ".\n\nAtentamente,\n" + invoice.getIssuerRut();
        String filename = "DTE_" + invoice.getFolio() + ".pdf";

        invoiceEmailSender.sendInvoiceEmail(recipient, subject, body, pdfContent, filename);

        auditTrailService.record(invoice.getCompanyId(), invoice.getId(), InvoiceAuditLog.ACTION_SENT,
                "Invoice folio " + invoice.getFolio() + " sent to " + recipient);
    }
}
