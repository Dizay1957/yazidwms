package com.yazidwms.report.dto;

public final class ReportDtos {
    private ReportDtos() {
    }

    public enum Format {
        CSV,
        EXCEL,
        PDF
    }

    public record ReportFile(String filename, String contentType, byte[] bytes) {
    }
}
