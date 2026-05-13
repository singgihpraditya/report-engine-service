package com.singgih.reportengineservice.service.report;

import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Renders a Thymeleaf TEXT-mode template and writes it as an XLSX workbook.
 *
 * Template convention (pipe {@code |} as column delimiter, newline as row delimiter):
 * <pre>
 *   Baris tanpa | → judul (sebelum header kolom) atau footer (sesudahnya), di-merge lintas kolom
 *   Baris pertama dengan | → header kolom (bold + background abu-abu)
 *   Baris berikutnya dengan | → baris data
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XlsxReportGenerator implements ReportGeneratorStrategy {

    private static final String COL_DELIMITER = "|";

    private final TemplateProcessingService templateProcessor;

    @Override
    public ReportType getSupportedType() {
        return ReportType.XLSX;
    }

    @Override
    public byte[] generate(String template, Map<String, Object> params) {
        String rendered = templateProcessor.processText(template, params);
        return buildXlsx(rendered);
    }

    private byte[] buildXlsx(String rendered) {
        String[] lines = rendered.split("\n");
        int maxCols = detectMaxCols(lines);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Report");

            CellStyle colHeaderStyle = buildColHeaderStyle(workbook);
            CellStyle titleStyle    = buildTitleStyle(workbook);
            CellStyle footerStyle   = buildFooterStyle(workbook);

            boolean columnHeaderWritten = false;
            int rowIndex = 0;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                Row row = sheet.createRow(rowIndex++);

                if (trimmed.contains(COL_DELIMITER)) {
                    // Data atau header kolom
                    String[] cols = trimmed.split("\\" + COL_DELIMITER, -1);
                    CellStyle style = columnHeaderWritten ? null : colHeaderStyle;
                    columnHeaderWritten = true;

                    for (int c = 0; c < cols.length; c++) {
                        Cell cell = row.createCell(c);
                        cell.setCellValue(cols[c].trim());
                        if (style != null) cell.setCellStyle(style);
                    }
                } else {
                    // Judul (sebelum header) atau footer (sesudah header)
                    CellStyle style = columnHeaderWritten ? footerStyle : titleStyle;
                    Cell cell = row.createCell(0);
                    cell.setCellValue(trimmed);
                    cell.setCellStyle(style);

                    if (maxCols > 1) {
                        sheet.addMergedRegion(
                                new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, maxCols - 1));
                    }
                }
            }

            for (int i = 0; i < maxCols; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new ReportGenerationException("Gagal membuat XLSX", e);
        }
    }

    /** Hitung jumlah kolom maksimum dari baris yang mengandung delimiter. */
    private int detectMaxCols(String[] lines) {
        int max = 1;
        for (String line : lines) {
            String t = line.trim();
            if (!t.isEmpty() && t.contains(COL_DELIMITER)) {
                max = Math.max(max, t.split("\\" + COL_DELIMITER, -1).length);
            }
        }
        return max;
    }

    private CellStyle buildColHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle buildTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle buildFooterStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setItalic(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
