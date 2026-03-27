package com.stockmaster.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "export")
public class ExportConfig {

    private String tempPath = "/tmp/export";
    private Integer maxRows = 100000;
    private Integer batchSize = 1000;
    private String dateFormat = "yyyy-MM-dd";
    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private ExcelConfig excel = new ExcelConfig();
    private PdfConfig pdf = new PdfConfig();

    @Data
    public static class ExcelConfig {
        private Integer headerRowHeight = 25;
        private Integer rowHeight = 20;
        private Integer defaultColumnWidth = 15;
        private Integer maxColumnWidth = 50;
        private Double autoWidthFactor = 1.2;
    }

    @Data
    public static class PdfConfig {
        private String pageSize = "A4";
        private Integer titleFontSize = 16;
        private Integer headerFontSize = 10;
        private Integer contentFontSize = 9;
        private Float marginTop = 20f;
        private Float marginRight = 15f;
        private Float marginBottom = 20f;
        private Float marginLeft = 15f;
    }

    public ExcelConfig getExcel() {
        return excel;
    }

    public PdfConfig getPdf() {
        return pdf;
    }
}
