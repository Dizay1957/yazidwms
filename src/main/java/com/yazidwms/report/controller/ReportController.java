package com.yazidwms.report.controller;

import com.yazidwms.report.dto.ReportDtos.Format;
import com.yazidwms.report.dto.ReportDtos.ReportFile;
import com.yazidwms.report.service.ReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory")
    ResponseEntity<byte[]> inventory(@RequestParam(defaultValue = "CSV") Format format) {
        return file(reportService.inventory(format));
    }

    @GetMapping("/low-stock")
    ResponseEntity<byte[]> lowStock(@RequestParam(defaultValue = "CSV") Format format) {
        return file(reportService.lowStock(format));
    }

    @GetMapping("/purchase")
    ResponseEntity<byte[]> purchase(@RequestParam(defaultValue = "CSV") Format format) {
        return file(reportService.purchase(format));
    }

    @GetMapping("/sales")
    ResponseEntity<byte[]> sales(@RequestParam(defaultValue = "CSV") Format format) {
        return file(reportService.sales(format));
    }

    @GetMapping("/stock-movements")
    ResponseEntity<byte[]> stockMovements(@RequestParam(defaultValue = "CSV") Format format) {
        return file(reportService.stockMovements(format));
    }

    private ResponseEntity<byte[]> file(ReportFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.filename()).build().toString())
                .body(file.bytes());
    }
}
