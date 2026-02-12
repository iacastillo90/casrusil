package com.casrusil.siierpai.modules.energy.application.service.reporting;

import com.casrusil.siierpai.modules.energy.domain.model.reporting.EnergyReport;
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
            // For now, generate a formatted text representation
            // In production, use a proper PDF library
            String content = generatePdfContent(report);

            return content.getBytes("UTF-8");

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
    private String generatePdfContent(EnergyReport report) {
        StringBuilder pdf = new StringBuilder();

        // Header
        pdf.append("═══════════════════════════════════════════════════════════\n");
        pdf.append("                    ENERGY REPORT\n");
        pdf.append("═══════════════════════════════════════════════════════════\n\n");

        // Report Info
        pdf.append("Report Type:     ").append(report.getType()).append("\n");
        pdf.append("Period:          ").append(report.getPeriod()).append("\n");
        pdf.append("Generated:       ").append(report.getGeneratedAt().format(DATETIME_FORMATTER)).append("\n");
        pdf.append("\n");

        // Summary Section
        pdf.append("───────────────────────────────────────────────────────────\n");
        pdf.append("                    SUMMARY\n");
        pdf.append("───────────────────────────────────────────────────────────\n\n");

        pdf.append(String.format("%-30s %15s\n", "Total Energy Consumed:",
                report.getTotalEnergyConsumed() + " kWh"));
        pdf.append(String.format("%-30s %15s\n", "Total Revenue:",
                "$" + report.getTotalRevenue()));
        pdf.append(String.format("%-30s %15s\n", "Total Cost:",
                "$" + report.getTotalCost()));
        pdf.append(String.format("%-30s %15s\n", "Net Profit:",
                "$" + report.getNetProfit()));
        pdf.append(String.format("%-30s %15s\n", "Profit Margin:",
                report.getProfitMargin() + "%"));
        pdf.append("\n");

        // Session Statistics
        pdf.append("───────────────────────────────────────────────────────────\n");
        pdf.append("                SESSION STATISTICS\n");
        pdf.append("───────────────────────────────────────────────────────────\n\n");

        pdf.append(String.format("%-30s %15s\n", "Total Sessions:",
                report.getSessionCount()));
        pdf.append(String.format("%-30s %15s\n", "Avg Energy/Session:",
                report.getAverageEnergyPerSession() + " kWh"));
        pdf.append(String.format("%-30s %15s\n", "Avg Revenue/Session:",
                "$" + report.getAverageRevenuePerSession()));

        if (report.getAverageSessionDuration() != null) {
            long hours = report.getAverageSessionDuration().toHours();
            long minutes = report.getAverageSessionDuration().toMinutesPart();
            pdf.append(String.format("%-30s %15s\n", "Avg Session Duration:",
                    hours + "h " + minutes + "m"));
        }
        pdf.append("\n");

        // Peak Usage Hours
        if (!report.getPeakUsageHours().isEmpty()) {
            pdf.append("───────────────────────────────────────────────────────────\n");
            pdf.append("                PEAK USAGE HOURS\n");
            pdf.append("───────────────────────────────────────────────────────────\n\n");

            pdf.append(String.format("%-10s %-20s %-15s\n", "Hour", "Energy (kWh)", "Sessions"));
            pdf.append("─────────────────────────────────────────────────────\n");

            report.getPeakUsageHours().forEach(hour -> {
                pdf.append(String.format("%-10s %-20s %-15s\n",
                        hour.hour() + ":00",
                        hour.energyConsumed(),
                        hour.sessionCount()));
            });
            pdf.append("\n");
        }

        // Top Stations
        if (!report.getTopStations().isEmpty()) {
            pdf.append("───────────────────────────────────────────────────────────\n");
            pdf.append("                TOP STATIONS\n");
            pdf.append("───────────────────────────────────────────────────────────\n\n");

            pdf.append(String.format("%-25s %-15s %-15s %-10s\n",
                    "Station", "Energy (kWh)", "Revenue", "Sessions"));
            pdf.append("─────────────────────────────────────────────────────────────────\n");

            report.getTopStations().forEach(station -> {
                pdf.append(String.format("%-25s %-15s %-15s %-10s\n",
                        station.stationName(),
                        station.energyConsumed(),
                        "$" + station.revenue(),
                        station.sessionCount()));
            });
            pdf.append("\n");
        }

        // Footer
        pdf.append("═══════════════════════════════════════════════════════════\n");
        pdf.append("                 End of Report\n");
        pdf.append("═══════════════════════════════════════════════════════════\n");

        return pdf.toString();
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
