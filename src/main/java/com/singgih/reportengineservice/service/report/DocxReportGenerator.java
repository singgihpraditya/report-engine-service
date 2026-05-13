package com.singgih.reportengineservice.service.report;

import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocxReportGenerator implements ReportGeneratorStrategy {

    private final TemplateProcessingService templateProcessor;

    @Override
    public ReportType getSupportedType() {
        return ReportType.DOCX;
    }

    @Override
    public byte[] generate(String template, Map<String, Object> params) {
        String html = templateProcessor.processHtml(template, params);
        return convertHtmlToDocx(html);
    }

    private byte[] convertHtmlToDocx(String html) {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            writeBody(doc, Jsoup.parse(html).body());
            doc.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new ReportGenerationException("Gagal membuat DOCX", e);
        }
    }

    private void writeBody(XWPFDocument doc, Element body) {
        for (Element el : body.children()) {
            switch (el.tagName().toLowerCase()) {
                case "h1" -> addHeading(doc, el.text(), 1);
                case "h2" -> addHeading(doc, el.text(), 2);
                case "h3" -> addHeading(doc, el.text(), 3);
                case "p"  -> addParagraph(doc, el.text());
                case "hr" -> addHorizontalRule(doc);
                case "table" -> addTable(doc, el);
                case "div", "header", "footer", "section", "main" -> writeBody(doc, el);
                default -> {
                    if (!el.text().isBlank()) addParagraph(doc, el.text());
                }
            }
        }
    }

    private void addHeading(XWPFDocument doc, String text, int level) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("Heading" + level);
        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setText(text);
    }

    private void addParagraph(XWPFDocument doc, String text) {
        doc.createParagraph().createRun().setText(text);
    }

    private void addHorizontalRule(XWPFDocument doc) {
        doc.createParagraph().setBorderBottom(Borders.SINGLE);
    }

    private void addTable(XWPFDocument doc, Element tableEl) {
        Elements rows = tableEl.select("tr");
        if (rows.isEmpty()) return;

        int colCount = rows.stream().mapToInt(r -> r.select("th, td").size()).max().orElse(1);
        XWPFTable table = doc.createTable(rows.size(), colCount);
        table.setStyleID("TableGrid");

        for (int r = 0; r < rows.size(); r++) {
            Elements cells = rows.get(r).select("th, td");
            boolean isHeaderRow = !rows.get(r).select("th").isEmpty();
            XWPFTableRow tableRow = table.getRow(r);

            for (int c = 0; c < cells.size(); c++) {
                XWPFTableCell cell = c < tableRow.getTableCells().size()
                        ? tableRow.getCell(c)
                        : tableRow.addNewTableCell();
                XWPFRun run = cell.getParagraphs().get(0).createRun();
                if (isHeaderRow) run.setBold(true);
                run.setText(cells.get(c).text());
            }
        }
    }
}
