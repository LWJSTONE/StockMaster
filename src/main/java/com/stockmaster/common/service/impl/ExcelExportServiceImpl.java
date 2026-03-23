package com.stockmaster.common.service.impl;

import com.stockmaster.common.config.ExportConfig;
import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.common.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Excel export service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExportService {

    private final ExportConfig exportConfig;

    @Override
    public void exportToExcel(String fileName, String sheetName, List<String> headers,
                              List<List<Object>> dataList, HttpServletResponse response) {
        // Validate data size
        if (dataList.size() > exportConfig.getMaxRows()) {
            throw new BusinessException("Export data exceeds maximum limit: " + exportConfig.getMaxRows());
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Create cell styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(exportConfig.getExcel().getHeaderRowHeight());
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, exportConfig.getExcel().getDefaultColumnWidth() * 256);
            }

            // Create data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < dataList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.setHeightInPoints(exportConfig.getExcel().getRowHeight());
                List<Object> rowData = dataList.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = rowData.get(j);
                    setCellValue(cell, value, dataStyle, dateStyle, formatter);
                }
            }

            // Auto-size columns
            autoSizeColumns(sheet, headers.size());

            // Set response headers
            setResponseHeaders(response, fileName + ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // Write to response
            workbook.write(response.getOutputStream());

        } catch (IOException e) {
            log.error("Excel export failed", e);
            throw new BusinessException("Excel export failed: " + e.getMessage());
        }
    }

    @Override
    public void exportToPdf(String fileName, String title, List<String> headers,
                            List<List<Object>> dataList, HttpServletResponse response) {
        throw new UnsupportedOperationException("Please use PdfExportServiceImpl for PDF export");
    }

    @Override
    public Map<String, Object> getExportData(String exportType, Map<String, Object> params) {
        throw new UnsupportedOperationException("Please use ExportController for data retrieval");
    }

    /**
     * Create header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        return style;
    }

    /**
     * Create data cell style
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        return style;
    }

    /**
     * Create date cell style
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    /**
     * Set cell value
     */
    private void setCellValue(Cell cell, Object value, CellStyle dataStyle, CellStyle dateStyle, DateTimeFormatter formatter) {
        if (value == null) {
            cell.setCellValue("");
            cell.setCellStyle(dataStyle);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
            cell.setCellStyle(dataStyle);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            cell.setCellStyle(dataStyle);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(formatter));
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(value.toString());
            cell.setCellStyle(dataStyle);
        }
    }

    /**
     * Auto-size columns
     */
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // Adjust column width
            int currentWidth = sheet.getColumnWidth(i);
            int maxWidth = exportConfig.getExcel().getMaxColumnWidth() * 256;
            if (currentWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            } else {
                sheet.setColumnWidth(i, (int) (currentWidth * exportConfig.getExcel().getAutoWidthFactor()));
            }
        }
    }

    /**
     * Set response headers
     */
    private void setResponseHeaders(HttpServletResponse response, String fileName, String contentType) throws IOException {
        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }
}
