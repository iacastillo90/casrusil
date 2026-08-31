package com.casrusil.siierpai.modules.energy.application.service.reporting;

import com.casrusil.siierpai.modules.energy.domain.model.reporting.EnergyReport;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Service for exporting reports to PDF format.
 * 
 * Uses a simple text-based PDF generation for now.
 * Can be enhanced with libraries like iText or Apache PDFBox.
 */
@Service
public class PdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Exports an energy report to PDF format.
     * 
     * Note: This is a simplified implementation using plain text.
     * For production, consider using iText or Apache PDFBox for proper PDF
     * generation.
     * 
     * @param report Energy report to export
     * @return PDF content as byte array
     */
    public byte[] exportToPdf(EnergyReport report) {
        logger.info("Exporting report {} to PDF", report.getId());

        try {
            // Generate PDF with OpenPDF
            byte[] content = generatePdfContent(report);

            return content;

        } catch (Exception e) {
            logger.error("Failed to export report to PDF: {}", e.getMessage());
            throw new RuntimeException("PDF export failed", e);
        }
    }

    /**
     * Generates PDF content as formatted text.
     * 
     * TODO: Replace with proper PDF library (iText, PDFBox, etc.)
     */
    private byte[] generatePdfContent(EnergyReport report) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // Title
        Paragraph title = new Paragraph("ENERGY REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // Report Info
        document.add(new Paragraph("Report Type: " + report.getType(), normalFont));
        document.add(new Paragraph("Period: " + report.getPeriod(), normalFont));
        document.add(new Paragraph("Generated: " + report.getGeneratedAt().format(DATETIME_FORMATTER), normalFont));
        document.add(new Paragraph(" "));

        // Summary Section
        document.add(new Paragraph("SUMMARY", headerFont));
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10f);
        summaryTable.setSpacingAfter(10f);

        addTableRow(summaryTable, "Total Energy Consumed:", report.getTotalEnergyConsumed() + " kWh", normalFont);
        addTableRow(summaryTable, "Total Revenue:", "$" + report.getTotalRevenue(), normalFont);
        addTableRow(summaryTable, "Total Cost:", "$" + report.getTotalCost(), normalFont);
        addTableRow(summaryTable, "Net Profit:", "$" + report.getNetProfit(), normalFont);
        addTableRow(summaryTable, "Profit Margin:", report.getProfitMargin() + "%", normalFont);
        document.add(summaryTable);

        // Session Statistics
        document.add(new Paragraph("SESSION STATISTICS", headerFont));
        PdfPTable sessionTable = new PdfPTable(2);
        sessionTable.setWidthPercentage(100);
        sessionTable.setSpacingBefore(10f);
        sessionTable.setSpacingAfter(10f);

        addTableRow(sessionTable, "Total Sessions:", String.valueOf(report.getSessionCount()), normalFont);
        addTableRow(sessionTable, "Avg Energy/Session:", report.getAverageEnergyPerSession() + " kWh", normalFont);
        addTableRow(sessionTable, "Avg Revenue/Session:", "$" + report.getAverageRevenuePerSession(), normalFont);

        if (report.getAverageSessionDuration() != null) {
            long hours = report.getAverageSessionDuration().toHours();
            long minutes = report.getAverageSessionDuration().toMinutesPart();
            addTableRow(sessionTable, "Avg Session Duration:", hours + "h " + minutes + "m", normalFont);
        }
        document.add(sessionTable);

        // Top Stations
        if (!report.getTopStations().isEmpty()) {
            document.add(new Paragraph("TOP STATIONS", headerFont));
            PdfPTable topStationsTable = new PdfPTable(4);
            topStationsTable.setWidthPercentage(100);
            topStationsTable.setSpacingBefore(10f);
            
            topStationsTable.addCell(new Phrase("Station", headerFont));
            topStationsTable.addCell(new Phrase("Energy (kWh)", headerFont));
            topStationsTable.addCell(new Phrase("Revenue", headerFont));
            topStationsTable.addCell(new Phrase("Sessions", headerFont));

            report.getTopStations().forEach(station -> {
                topStationsTable.addCell(new Phrase(station.stationName(), normalFont));
                topStationsTable.addCell(new Phrase(String.valueOf(station.energyConsumed()), normalFont));
                topStationsTable.addCell(new Phrase("$" + station.revenue(), normalFont));
                topStationsTable.addCell(new Phrase(String.valueOf(station.sessionCount()), normalFont));
            });
            document.add(topStationsTable);
        }

        document.close();
        return baos.toByteArray();
    }

    private void addTableRow(PdfPTable table, String key, String value, Font font) {
        PdfPCell cell1 = new PdfPCell(new Phrase(key, font));
        cell1.setBorder(0);
        PdfPCell cell2 = new PdfPCell(new Phrase(value, font));
        cell2.setBorder(0);
        table.addCell(cell1);
        table.addCell(cell2);
    }

    /**
     * Gets the filename for the PDF export.
     */
    public String getFilename(EnergyReport report) {
        return String.format("energy_report_%s_%s.pdf",
                report.getType().toString().toLowerCase(),
                report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
    }
}
