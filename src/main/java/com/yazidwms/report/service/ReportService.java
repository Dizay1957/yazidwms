package com.yazidwms.report.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import com.yazidwms.product.entity.Product;
import com.yazidwms.product.repository.ProductRepository;
import com.yazidwms.purchaseorder.entity.PurchaseOrder;
import com.yazidwms.purchaseorder.repository.PurchaseOrderRepository;
import com.yazidwms.report.dto.ReportDtos.Format;
import com.yazidwms.report.dto.ReportDtos.ReportFile;
import com.yazidwms.salesorder.entity.SalesOrder;
import com.yazidwms.salesorder.repository.SalesOrderRepository;
import com.yazidwms.stockmovement.entity.StockMovement;
import com.yazidwms.stockmovement.repository.StockMovementRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final Color BRAND = new Color(37, 99, 235);
    private static final Color HEADER = new Color(15, 23, 42);
    private static final String EXCEL_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final StockMovementRepository stockMovementRepository;

    public ReportService(ProductRepository productRepository, PurchaseOrderRepository purchaseOrderRepository,
                         SalesOrderRepository salesOrderRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public ReportFile inventory(Format format) {
        var products = productRepository.findAll().stream()
                .filter(product -> !product.isDeleted())
                .toList();
        var report = new TabularReport(
                "Inventory Valuation",
                "Current stock, replenishment thresholds, purchase prices, and inventory value.",
                List.of("SKU", "Product", "Category", "Supplier", "Quantity", "Minimum", "Purchase Price", "Inventory Value"),
                products.stream().map(this::inventoryRow).toList()
        );
        return export("inventory-valuation", report, format);
    }

    @Transactional(readOnly = true)
    public ReportFile lowStock(Format format) {
        var products = productRepository.findAll().stream()
                .filter(product -> !product.isDeleted() && product.isLowStock())
                .toList();
        var report = new TabularReport(
                "Low Stock",
                "Products at or below their configured minimum quantity.",
                List.of("SKU", "Product", "Quantity", "Minimum", "Shortage", "Supplier", "Inventory Value"),
                products.stream().map(this::lowStockRow).toList()
        );
        return export("low-stock", report, format);
    }

    @Transactional(readOnly = true)
    public ReportFile purchase(Format format) {
        var rows = new ArrayList<List<String>>();
        for (var order : purchaseOrderRepository.findByDeletedFalseOrderByCreatedAtDesc()) {
            rows.addAll(purchaseRows(order));
        }
        var report = new TabularReport(
                "Purchase Orders",
                "Inbound purchase order lines with supplier, destination bin, status, and totals.",
                List.of("Order", "Status", "Supplier", "Created", "Received", "SKU", "Product", "Bin", "Qty", "Unit Price", "Line Total"),
                rows
        );
        return export("purchase-orders", report, format);
    }

    @Transactional(readOnly = true)
    public ReportFile sales(Format format) {
        var rows = new ArrayList<List<String>>();
        for (var order : salesOrderRepository.findByDeletedFalseOrderByCreatedAtDesc()) {
            rows.addAll(salesRows(order));
        }
        var report = new TabularReport(
                "Sales Orders",
                "Outbound sales order lines with customer, source bin, status, and totals.",
                List.of("Order", "Status", "Customer", "Created", "Shipped", "SKU", "Product", "Bin", "Qty", "Unit Price", "Line Total"),
                rows
        );
        return export("sales-orders", report, format);
    }

    @Transactional(readOnly = true)
    public ReportFile stockMovements(Format format) {
        var report = new TabularReport(
                "Stock Movements",
                "Immutable inventory movement history for receiving, shipping, transfers, and adjustments.",
                List.of("Timestamp", "Type", "Reference", "SKU", "Product", "From Bin", "To Bin", "Qty", "Previous", "New", "Reason", "Performed By"),
                stockMovementRepository.findByDeletedFalseOrderByTimestampDesc().stream().map(this::movementRow).toList()
        );
        return export("stock-movements", report, format);
    }

    private ReportFile export(String slug, TabularReport report, Format format) {
        var datedName = slug + "-" + DateTimeFormatter.ISO_LOCAL_DATE.format(java.time.LocalDate.now());
        return switch (format) {
            case CSV -> csv(datedName, report);
            case EXCEL -> excel(datedName, report);
            case PDF -> pdf(datedName, report);
        };
    }

    private List<String> inventoryRow(Product product) {
        return List.of(
                value(product.getSku()),
                value(product.getName()),
                product.getCategory() == null ? "" : value(product.getCategory().getName()),
                product.getSupplier() == null ? "" : value(product.getSupplier().getCompanyName()),
                String.valueOf(product.getQuantity()),
                String.valueOf(product.getMinimumQuantity()),
                money(product.getPurchasePrice()),
                money(inventoryValue(product))
        );
    }

    private List<String> lowStockRow(Product product) {
        return List.of(
                value(product.getSku()),
                value(product.getName()),
                String.valueOf(product.getQuantity()),
                String.valueOf(product.getMinimumQuantity()),
                String.valueOf(Math.max(product.getMinimumQuantity() - product.getQuantity(), 0)),
                product.getSupplier() == null ? "" : value(product.getSupplier().getCompanyName()),
                money(inventoryValue(product))
        );
    }

    private List<List<String>> purchaseRows(PurchaseOrder order) {
        if (order.getItems().isEmpty()) {
            return List.of(List.of(value(order.getOrderNumber()), order.getStatus().name(), supplierName(order), date(order.getCreatedAt()),
                    date(order.getReceivedAt()), "", "", "", "0", "", money(order.getTotalAmount())));
        }
        return order.getItems().stream().map(item -> List.of(
                value(order.getOrderNumber()),
                order.getStatus().name(),
                supplierName(order),
                date(order.getCreatedAt()),
                date(order.getReceivedAt()),
                value(item.getProduct().getSku()),
                value(item.getProduct().getName()),
                value(item.getBin().getCode()),
                String.valueOf(item.getQuantity()),
                money(item.getUnitPrice()),
                money(item.getLineTotal())
        )).toList();
    }

    private List<List<String>> salesRows(SalesOrder order) {
        if (order.getItems().isEmpty()) {
            return List.of(List.of(value(order.getOrderNumber()), order.getStatus().name(), customerName(order), date(order.getCreatedAt()),
                    date(order.getShippedAt()), "", "", "", "0", "", money(order.getTotalAmount())));
        }
        return order.getItems().stream().map(item -> List.of(
                value(order.getOrderNumber()),
                order.getStatus().name(),
                customerName(order),
                date(order.getCreatedAt()),
                date(order.getShippedAt()),
                value(item.getProduct().getSku()),
                value(item.getProduct().getName()),
                value(item.getBin().getCode()),
                String.valueOf(item.getQuantity()),
                money(item.getUnitPrice()),
                money(item.getLineTotal())
        )).toList();
    }

    private List<String> movementRow(StockMovement movement) {
        return List.of(
                date(movement.getTimestamp()),
                movement.getType().name(),
                value(movement.getReference()),
                value(movement.getProduct().getSku()),
                value(movement.getProduct().getName()),
                movement.getFromBin() == null ? "" : value(movement.getFromBin().getCode()),
                movement.getToBin() == null ? "" : value(movement.getToBin().getCode()),
                String.valueOf(movement.getQuantity()),
                String.valueOf(movement.getPreviousQuantity()),
                String.valueOf(movement.getNewQuantity()),
                value(movement.getReason()),
                movement.getPerformedBy() == null ? "System" : value(movement.getPerformedBy().getFullName())
        );
    }

    private ReportFile csv(String name, TabularReport report) {
        try (var out = new ByteArrayOutputStream(); var writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);
            writer.writeNext(new String[]{report.title()});
            writer.writeNext(new String[]{"Generated", date(Instant.now())});
            writer.writeNext(new String[]{report.subtitle()});
            writer.writeNext(new String[]{});
            writer.writeNext(report.headers().toArray(String[]::new));
            for (var row : report.rows()) {
                writer.writeNext(row.toArray(String[]::new));
            }
            writer.flush();
            return new ReportFile(name + ".csv", "text/csv; charset=UTF-8", out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate CSV report", ex);
        }
    }

    private ReportFile excel(String name, TabularReport report) {
        try (Workbook workbook = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Report");
            var title = titleStyle(workbook);
            var meta = metaStyle(workbook);
            var header = headerStyle(workbook);
            var body = bodyStyle(workbook);

            var titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(28);
            titleRow.createCell(0).setCellValue("YazidWMS - " + report.title());
            titleRow.getCell(0).setCellStyle(title);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, Math.max(report.headers().size() - 1, 0)));

            var metaRow = sheet.createRow(1);
            metaRow.createCell(0).setCellValue(report.subtitle());
            metaRow.getCell(0).setCellStyle(meta);
            metaRow.createCell(1).setCellValue("Generated: " + date(Instant.now()));
            metaRow.getCell(1).setCellStyle(meta);

            var headerRow = sheet.createRow(3);
            for (int i = 0; i < report.headers().size(); i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(report.headers().get(i));
                cell.setCellStyle(header);
            }

            for (int rowIndex = 0; rowIndex < report.rows().size(); rowIndex++) {
                var row = sheet.createRow(rowIndex + 4);
                var values = report.rows().get(rowIndex);
                for (int col = 0; col < values.size(); col++) {
                    var cell = row.createCell(col);
                    cell.setCellValue(values.get(col));
                    cell.setCellStyle(body);
                }
            }

            sheet.createFreezePane(0, 4);
            sheet.setAutoFilter(new CellRangeAddress(3, Math.max(3, report.rows().size() + 3), 0, report.headers().size() - 1));
            for (int i = 0; i < report.headers().size(); i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 800, 10000));
            }
            workbook.write(out);
            return new ReportFile(name + ".xlsx", EXCEL_TYPE, out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate Excel report", ex);
        }
    }

    private ReportFile pdf(String name, TabularReport report) {
        try (var out = new ByteArrayOutputStream()) {
            var document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(document, out);
            document.open();

            var title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND);
            var subtitle = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            document.add(new Paragraph("YazidWMS - " + report.title(), title));
            document.add(new Paragraph(report.subtitle(), subtitle));
            document.add(new Paragraph("Generated: " + date(Instant.now()), subtitle));
            document.add(new Paragraph(" "));

            var table = new PdfPTable(report.headers().size());
            table.setWidthPercentage(100);
            for (var heading : report.headers()) {
                table.addCell(pdfHeader(heading));
            }
            if (report.rows().isEmpty()) {
                var empty = new PdfPCell(new Phrase("No records found for this report."));
                empty.setColspan(report.headers().size());
                empty.setPadding(10);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(empty);
            } else {
                for (var row : report.rows()) {
                    for (var value : row) {
                        table.addCell(pdfBody(value));
                    }
                }
            }
            document.add(table);
            document.close();
            return new ReportFile(name + ".pdf", "application/pdf", out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate PDF report", ex);
        }
    }

    private CellStyle titleStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        var font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle metaStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        var font = workbook.createFont();
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle headerStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        var font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle bodyStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private PdfPCell pdfHeader(String text) {
        var cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        cell.setBackgroundColor(HEADER);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell pdfBody(String text) {
        var cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK)));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private BigDecimal inventoryValue(Product product) {
        return product.getPurchasePrice().multiply(BigDecimal.valueOf(product.getQuantity()));
    }

    private String supplierName(PurchaseOrder order) {
        return order.getSupplier() == null ? "" : value(order.getSupplier().getCompanyName());
    }

    private String customerName(SalesOrder order) {
        if (order.getCustomer() == null) {
            return "";
        }
        return value(order.getCustomer().getCompanyName() == null || order.getCustomer().getCompanyName().isBlank()
                ? order.getCustomer().getFullName()
                : order.getCustomer().getCompanyName());
    }

    private String money(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String date(Instant instant) {
        return instant == null ? "" : DATE_TIME.format(instant);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private record TabularReport(String title, String subtitle, List<String> headers, List<List<String>> rows) {
    }
}
