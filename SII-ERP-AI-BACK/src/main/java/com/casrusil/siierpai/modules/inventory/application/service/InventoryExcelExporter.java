package com.casrusil.siierpai.modules.inventory.application.service;

import com.casrusil.siierpai.modules.inventory.domain.model.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class InventoryExcelExporter {

    public byte[] export(List<Product> products) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventario Valorizado");
            
            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "SKU", "Nombre", "Cantidad", "Costo Unitario (PMP)", "Total Valorizado"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }
            
            // Data Rows
            int rowIdx = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId() != null ? p.getId() : 0);
                row.createCell(1).setCellValue(p.getSku());
                row.createCell(2).setCellValue(p.getName());
                row.createCell(3).setCellValue(p.getQuantity());
                row.createCell(4).setCellValue(p.getUnitCost() != null ? p.getUnitCost().doubleValue() : 0);
                row.createCell(5).setCellValue(p.getTotalValue() != null ? p.getTotalValue().doubleValue() : 0);
            }
            
            // Autosize
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}
