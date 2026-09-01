package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.mail.EmailService;
import com.casrusil.siierpai.shared.infrastructure.reporting.ExcelExportService;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class WeeklyReportService {

    private final SearchInvoicesUseCase searchInvoicesUseCase;
    private final BalanceSheetService balanceSheetService;
    private final EmailService emailService;

    public WeeklyReportService(SearchInvoicesUseCase searchInvoicesUseCase, BalanceSheetService balanceSheetService,
            EmailService emailService) {
        this.searchInvoicesUseCase = searchInvoicesUseCase;
        this.balanceSheetService = balanceSheetService;
        this.emailService = emailService;
    }

    public void generateAndSendWeeklyReport(CompanyId companyId, String userEmail) {
        // 1. Fetch Data
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(7);

        List<Invoice> invoices = searchInvoicesUseCase.getInvoicesByCompany(companyId); // In prod: filter by date in DB
        List<Invoice> weeklyInvoices = invoices.stream()
                .filter(i -> !i.getDate().isBefore(weekStart) && !i.getDate().isAfter(now))
                .toList();

        long salesCount = weeklyInvoices.stream().filter(i -> i.getTransactionType() == TransactionType.SALE).count();
        long purchasesCount = weeklyInvoices.stream().filter(i -> i.getTransactionType() == TransactionType.PURCHASE)
                .count();

        // 2. Generate PDF (Stub)
        byte[] pdfReport = generatePdfReport(salesCount, purchasesCount, weekStart, now);

        // 3. Send Email
        emailService.sendWeeklyReportEmail(userEmail, pdfReport, weekStart + " to " + now);
    }

    /**
     * Genera un PDF real del reporte semanal usando OpenPDF (reemplaza el stub que
     * devolvia bytes de texto plano).
     */
    private byte[] generatePdfReport(long sales, long purchases, LocalDate start, LocalDate end) {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("Reporte Semanal de Actividad", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            Paragraph period = new Paragraph("Periodo: " + start + " al " + end, textFont);
            period.setSpacingAfter(15f);
            document.add(period);

            document.add(buildLine("Documentos de venta emitidos: ", String.valueOf(sales), labelFont, textFont));
            document.add(buildLine("Documentos de compra recibidos: ", String.valueOf(purchases), labelFont, textFont));
            document.add(buildLine("Total documentos de la semana: ", String.valueOf(sales + purchases), labelFont, textFont));

            Paragraph footer = new Paragraph("Generado automaticamente por SII-ERP.", textFont);
            footer.setSpacingBefore(30f);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            if (document.isOpen()) {
                document.close();
            }
            throw new RuntimeException("No se pudo generar el PDF del reporte semanal: " + e.getMessage(), e);
        }
    }

    private Paragraph buildLine(String label, String value, Font labelFont, Font textFont) {
        Paragraph p = new Paragraph();
        p.add(new com.lowagie.text.Chunk(label, labelFont));
        p.add(new com.lowagie.text.Chunk(value, textFont));
        p.setSpacingAfter(8f);
        return p;
    }
}
