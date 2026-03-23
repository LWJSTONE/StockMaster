package com.stockmaster.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Export configuration class
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "export")
public class ExportConfig {

    /**
     * Export file storage path
     */
    private String tempPath = "/tmp/exports";

    /**
     * Maximum export rows
     */
    private Integer maxRows = 100000;

    /**
     * PDF export settings
     */
    private PdfConfig pdf = new PdfConfig();

    /**
     * Excel export settings
     */
    private ExcelConfig excel = new ExcelConfig();

    @Data
    public static class PdfConfig {
        /**
         * Page size
         */
        private String pageSize = "A4";

        /**
         * Font path
         */
        private String fontPath = "/fonts/simsun.ttc";

        /**
         * Title font size
         */
        private Integer titleFontSize = 16;

        /**
         * Header font size
         */
        private Integer headerFontSize = 12;

        /**
         * Content font size
         */
        private Integer contentFontSize = 10;

        /**
         * Page margins
         */
        private Float marginLeft = 36f;
        private Float marginRight = 36f;
        private Float marginTop = 36f;
        private Float marginBottom = 36f;
    }

    @Data
    public static class ExcelConfig {
        /**
         * Row height
         */
        private Short rowHeight = 20;

        /**
         * Header row height
         */
        private Short headerRowHeight = 25;

        /**
         * Auto column width factor
         */
        private Double autoWidthFactor = 1.5;

        /**
         * Maximum column width
         */
        private Integer maxColumnWidth = 50;

        /**
         * Default column width
         */
        private Integer defaultColumnWidth = 20;
    }
}
