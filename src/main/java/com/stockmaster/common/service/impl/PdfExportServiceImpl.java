package com.stockmaster.common.service.impl;

import com.stockmaster.common.config.ExportConfig;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.common.service.ExportService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PDF export service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements ExportService {

    private final ExportConfig exportConfig;

    @Override
    public void exportToExcel(String fileName, String sheetName, List<String> headers,
                              List<List<Object>> dataList, HttpServletResponse response) {
        throw new UnsupportedOperationException("Please use ExcelExportServiceImpl for Excel export");
    }

    @Override
    public void exportToPdf(String fileName, String title, List<String> headers,
                            List<List<Object>> dataList, HttpServletResponse response) {
        // Validate data size
        if (dataList.size() > exportConfig.getMaxRows()) {
            throw new BusinessException("Export data exceeds maximum limit: " + exportConfig.getMaxRows());
        }

        try {
            // Set response headers
            setResponseHeaders(response, fileName + ".pdf");

            // Create PDF document
            PdfWriter writer = new PdfWriter(response.getOutputStream());
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, getPageSize());

            // Set margins
            document.setMargins(
                    exportConfig.getPdf().getMarginTop(),
                    exportConfig.getPdf().getMarginRight(),
                    exportConfig.getPdf().getMarginBottom(),
                    exportConfig.getPdf().getMarginLeft()
            );

            // Create font
            PdfFont font = createFont();

            // Add title
            if (title != null && !title.isEmpty()) {
                Paragraph titleParagraph = new Paragraph(title)
                        .setFont(font)
                        .setFontSize(exportConfig.getPdf().getTitleFontSize())
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(titleParagraph);
            }

            // Create table
            float[] columnWidths = calculateColumnWidths(headers.size());
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .setWidth(UnitValue.createPercentValue(100));

            // Add header row
            for (String header : headers) {
                Cell cell = createHeaderCell(header, font);
                table.addHeaderCell(cell);
            }

            // Add data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (List<Object> rowData : dataList) {
                for (Object value : rowData) {
                    Cell cell = createDataCell(value, font, formatter);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

        } catch (IOException e) {
            log.error("PDF export failed", e);
            throw new BusinessException("PDF export failed: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getExportData(String exportType, Map<String, Object> params) {
        throw new UnsupportedOperationException("Please use ExportController for data retrieval");
    }

    /**
     * Get page size
     */
    private PageSize getPageSize() {
        String pageSize = exportConfig.getPdf().getPageSize();
        return switch (pageSize.toUpperCase()) {
            case "A3" -> PageSize.A3;
            case "A4" -> PageSize.A4;
            case "A5" -> PageSize.A5;
            case "LETTER" -> PageSize.LETTER;
            default -> PageSize.A4;
        };
    }

    /**
     * Create PDF font
     */
    private PdfFont createFont() throws IOException {
        try {
            // Try to use built-in font for Chinese characters
            return PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
        } catch (Exception e) {
            log.warn("Failed to load Chinese font, using default font: {}", e.getMessage());
            return PdfFontFactory.createFont();
        }
    }

    /**
     * Calculate column widths
     */
    private float[] calculateColumnWidths(int columnCount) {
        float[] widths = new float[columnCount];
        for (int i = 0; i < columnCount; i++) {
            widths[i] = 100f / columnCount;
        }
        return widths;
    }

    /**
     * Create header cell
     */
    private Cell createHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "")
                        .setFont(font)
                        .setFontSize(exportConfig.getPdf().getHeaderFontSize())
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(new DeviceRgb(66, 139, 202))
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER);
    }

    /**
     * Create data cell
     */
    private Cell createDataCell(Object value, PdfFont font, DateTimeFormatter formatter) {
        String displayValue;
        if (value == null) {
            displayValue = "";
        } else if (value instanceof LocalDateTime) {
            displayValue = ((LocalDateTime) value).format(formatter);
        } else if (value instanceof Number) {
            displayValue = value.toString();
        } else {
            displayValue = value.toString();
        }

        return new Cell()
                .add(new Paragraph(displayValue)
                        .setFont(font)
                        .setFontSize(exportConfig.getPdf().getContentFontSize())
                        .setTextAlignment(TextAlignment.CENTER))
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER);
    }

    /**
     * Set response headers
     */
    private void setResponseHeaders(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/pdf");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }
}
