package com.yazidwms.report.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import com.yazidwms.product.entity.Product;
import com.yazidwms.product.repository.ProductRepository;
import com.yazidwms.report.dto.ReportDtos.Format;
import com.yazidwms.report.dto.ReportDtos.ReportFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ReportService {

    private final ProductRepository productRepository;

    public ReportService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ReportFile inventory(Format format) {
        return productReport("inventory-report", productRepository.findAll(), format);
    }

    @Transactional(readOnly = true)
    public ReportFile lowStock(Format format) {
        var products = productRepository.findAll().stream().filter(product -> !product.isDeleted() && product.isLowStock()).toList();
        return productReport("low-stock-report", products, format);
    }

    @Transactional(readOnly = true)
    public ReportFile purchase(Format format) {
        return genericReport("purchase-report", "Purchase report generated from purchase-order endpoints.", format);
    }

    @Transactional(readOnly = true)
    public ReportFile sales(Format format) {
        return genericReport("sales-report", "Sales report generated from sales-order endpoints.", format);
    }

    @Transactional(readOnly = true)
    public ReportFile stockMovements(Format format) {
        return genericReport("stock-movement-report", "Stock movement report generated from immutable movement records.", format);
    }

    private ReportFile productReport(String name, List<Product> products, Format format) {
        return switch (format) {
            case CSV -> csv(name, products);
            case EXCEL -> excel(name, products);
            case PDF -> pdf(name, products);
        };
    }

    private ReportFile genericReport(String name, String message, Format format) {
        return switch (format) {
            case CSV -> new ReportFile(name + ".csv", "text/csv", ("title,message\n" + name + "," + message + "\n").getBytes(StandardCharsets.UTF_8));
            case EXCEL -> genericExcel(name, message);
            case PDF -> genericPdf(name, message);
        };
    }

    private ReportFile csv(String name, List<Product> products) {
        try (var out = new ByteArrayOutputStream(); var writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"SKU", "Name", "Quantity", "Minimum", "Purchase Price", "Inventory Value"});
            for (var product : products) {
                writer.writeNext(new String[]{
                        product.getSku(),
                        product.getName(),
                        String.valueOf(product.getQuantity()),
                        String.valueOf(product.getMinimumQuantity()),
                        product.getPurchasePrice().toPlainString(),
                        product.getPurchasePrice().multiply(java.math.BigDecimal.valueOf(product.getQuantity())).toPlainString()
                });
            }
            writer.flush();
            return new ReportFile(name + ".csv", "text/csv", out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate CSV report", ex);
        }
    }

    private ReportFile excel(String name, List<Product> products) {
        try (Workbook workbook = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Products");
            var header = sheet.createRow(0);
            var headers = List.of("SKU", "Name", "Quantity", "Minimum", "Purchase Price", "Inventory Value");
            for (int i = 0; i < headers.size(); i++) {
                header.createCell(i).setCellValue(headers.get(i));
            }
            for (int i = 0; i < products.size(); i++) {
                var product = products.get(i);
                var row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(product.getSku());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getQuantity());
                row.createCell(3).setCellValue(product.getMinimumQuantity());
                row.createCell(4).setCellValue(product.getPurchasePrice().doubleValue());
                row.createCell(5).setCellValue(product.getPurchasePrice().multiply(java.math.BigDecimal.valueOf(product.getQuantity())).doubleValue());
            }
            workbook.write(out);
            return new ReportFile(name + ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate Excel report", ex);
        }
    }

    private ReportFile pdf(String name, List<Product> products) {
        try (var out = new ByteArrayOutputStream()) {
            var document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("YazidWMS " + name));
            var table = new PdfPTable(5);
            List.of("SKU", "Name", "Qty", "Min", "Value").forEach(table::addCell);
            for (var product : products) {
                table.addCell(product.getSku());
                table.addCell(product.getName());
                table.addCell(String.valueOf(product.getQuantity()));
                table.addCell(String.valueOf(product.getMinimumQuantity()));
                table.addCell(product.getPurchasePrice().multiply(java.math.BigDecimal.valueOf(product.getQuantity())).toPlainString());
            }
            document.add(table);
            document.close();
            return new ReportFile(name + ".pdf", "application/pdf", out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate PDF report", ex);
        }
    }

    private ReportFile genericExcel(String name, String message) {
        try (Workbook workbook = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Report");
            sheet.createRow(0).createCell(0).setCellValue(name);
            sheet.createRow(1).createCell(0).setCellValue(message);
            workbook.write(out);
            return new ReportFile(name + ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate Excel report", ex);
        }
    }

    private ReportFile genericPdf(String name, String message) {
        try (var out = new ByteArrayOutputStream()) {
            var document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph(name));
            document.add(new Paragraph(message));
            document.close();
            return new ReportFile(name + ".pdf", "application/pdf", out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate PDF report", ex);
        }
    }
}
