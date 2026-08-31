package com.casrusil.siierpai.modules.payroll.application.service;

import com.casrusil.siierpai.modules.payroll.domain.model.Payslip;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PayslipPdfExporter {

    public byte[] export(Payslip payslip) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Liquidación de Sueldo", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Periodo: " + payslip.getPeriodId()));
            document.add(new Paragraph("Empleado ID: " + payslip.getEmployeeId()));
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Sueldo Base: $" + payslip.getBaseSalary()));
            document.add(new Paragraph("Gratificación: $" + payslip.getGratification()));
            document.add(new Paragraph("Total Imponible: $" + payslip.getTaxableIncome()));
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Descuentos Legales:"));
            document.add(new Paragraph("AFP: $" + payslip.getAfpDiscount()));
            document.add(new Paragraph("Salud: $" + payslip.getHealthDiscount()));
            document.add(new Paragraph("Impuesto Único: $" + payslip.getSingleTax()));
            
            document.add(new Paragraph(" "));
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("Sueldo Líquido a Pagar: $" + payslip.getNetSalary(), totalFont));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
