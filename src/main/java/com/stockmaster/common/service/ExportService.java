package com.stockmaster.common.service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Export service interface
 */
public interface ExportService {

    /**
     * Export data to Excel
     *
     * @param fileName  File name
     * @param sheetName Sheet name
     * @param headers   Headers
     * @param dataList  Data list
     * @param response  HTTP response
     */
    void exportToExcel(String fileName, String sheetName, List<String> headers,
                      List<List<Object>> dataList, HttpServletResponse response);

    /**
     * Export data to PDF
     *
     * @param fileName File name
     * @param title    Title
     * @param headers  Headers
     * @param dataList Data list
     * @param response HTTP response
     */
    void exportToPdf(String fileName, String title, List<String> headers,
                    List<List<Object>> dataList, HttpServletResponse response);

    /**
     * Get export data by type
     *
     * @param exportType Export type
     * @param params     Query parameters
     * @return Export data map
     */
    Map<String, Object> getExportData(String exportType, Map<String, Object> params);
}
