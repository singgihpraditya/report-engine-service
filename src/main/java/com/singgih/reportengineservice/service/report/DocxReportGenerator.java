package com.singgih.reportengineservice.service.report;

import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocxReportGenerator implements ReportGeneratorStrategy {

    private static final String FONT        = "Arial";
    private static final int    SIZE_BODY   = 10;
    private static final int    SIZE_TITLE  = 16;
    private static final int    SIZE_H3     = 13;
    private static final String COLOR_GREY  = "E0E0E0";

    private final TemplateProcessingService templateProcessor;

    @Override
    public ReportType getSupportedType() { return ReportType.DOCX; }

    @Override
    public byte[] generate(String template, Map<String, Object> params) {
        String html = templateProcessor.processHtml(template, params);
        return buildDocx(html);
    }

    // -------------------------------------------------------------------------
    // Top-level build
    // -------------------------------------------------------------------------

    private byte[] buildDocx(String html) {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            setPageMargins(doc);
            writeBody(doc, Jsoup.parse(html).body(), false);
            doc.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new ReportGenerationException("Gagal membuat DOCX", e);
        }
    }

    /** Set page margins to ~2 cm on all sides (matches CSS margin: 30px). */
    private void setPageMargins(XWPFDocument doc) {
        try {
            CTBody body = doc.getDocument().getBody();
            CTSectPr sectPr = body.getSectPr();
            if (sectPr == null) sectPr = body.addNewSectPr();
            CTPageMar pgMar = sectPr.getPgMar();
            if (pgMar == null) pgMar = sectPr.addNewPgMar();
            BigInteger margin = BigInteger.valueOf(1134); // ~2 cm in twentieths-of-a-point
            pgMar.setTop(margin);
            pgMar.setBottom(margin);
            pgMar.setLeft(margin);
            pgMar.setRight(margin);
        } catch (Exception e) {
            log.debug("setPageMargins skipped: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // HTML → DOCX traversal
    // -------------------------------------------------------------------------

    /**
     * Recursively walks HTML elements and writes them to the document.
     *
     * @param centered true when inside a .header or .footer div — centres all paragraphs
     */
    private void writeBody(XWPFDocument doc, Element parent, boolean centered) {
        for (Element el : parent.children()) {
            switch (el.tagName().toLowerCase()) {
                case "h1" -> addHeading(doc, el.text(), 1, centered);
                case "h2" -> addHeading(doc, el.text(), 2, centered);
                case "h3" -> addHeading(doc, el.text(), 3, centered);
                case "p"  -> addMixedParagraph(doc, el, centered);
                case "hr" -> addHorizontalRule(doc, centered);
                case "table" -> addTable(doc, el);
                case "div", "header", "footer", "section", "main" -> {
                    boolean childCentered = centered
                            || el.hasClass("header")
                            || el.hasClass("footer");
                    writeBody(doc, el, childCentered);
                }
                default -> {
                    if (!el.text().isBlank()) addSimpleParagraph(doc, el.text(), centered);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Paragraph helpers
    // -------------------------------------------------------------------------

    private void addHeading(XWPFDocument doc, String text, int level, boolean centered) {
        int size = switch (level) {
            case 1 -> 18;
            case 2 -> SIZE_TITLE;
            default -> SIZE_H3;
        };
        XWPFParagraph p = doc.createParagraph();
        if (centered) p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setFontFamily(FONT);
        run.setFontSize(size);
        run.setText(text);
    }

    private void addSimpleParagraph(XWPFDocument doc, String text, boolean centered) {
        XWPFParagraph p = doc.createParagraph();
        if (centered) p.setAlignment(ParagraphAlignment.CENTER);
        styleRun(p.createRun(), false, SIZE_BODY).setText(text);
    }

    /**
     * Handles inline-mixed paragraphs: plain text nodes, {@code <img>}, {@code <span>}.
     * Used for every {@code <p>} element.
     */
    private void addMixedParagraph(XWPFDocument doc, Element el, boolean centered) {
        XWPFParagraph para = doc.createParagraph();
        if (centered) para.setAlignment(ParagraphAlignment.CENTER);

        for (Node child : el.childNodes()) {
            if (child instanceof TextNode tn) {
                String text = tn.text();
                if (!text.isBlank()) styleRun(para.createRun(), false, SIZE_BODY).setText(text);

            } else if (child instanceof Element childEl) {
                switch (childEl.tagName().toLowerCase()) {
                    case "img"  -> addImageToParagraph(para, childEl);
                    case "span" -> {
                        boolean isTitle = childEl.hasClass("company-name");
                        styleRun(para.createRun(), isTitle, isTitle ? SIZE_TITLE : SIZE_BODY)
                                .setText(childEl.text());
                    }
                    default -> styleRun(para.createRun(), false, SIZE_BODY).setText(childEl.text());
                }
            }
        }
    }

    private void addImageToParagraph(XWPFParagraph para, Element imgEl) {
        String src = imgEl.attr("src");
        if (src.isBlank() || !src.startsWith("data:image/")) return;
        try {
            String[] parts = src.split(",", 2);
            if (parts.length < 2) return;
            byte[] imgBytes = Base64.getDecoder().decode(parts[1]);
            int type = parts[0].contains("png") ? XWPFDocument.PICTURE_TYPE_PNG
                                                 : XWPFDocument.PICTURE_TYPE_JPEG;
            para.createRun().addPicture(
                    new ByteArrayInputStream(imgBytes), type, "logo",
                    Units.pixelToEMU(36), Units.pixelToEMU(36));
        } catch (Exception e) {
            log.warn("Gagal menyisipkan gambar ke DOCX: {}", e.getMessage());
        }
    }

    /** Horizontal rule rendered as a paragraph bottom-border (high-level API). */
    private void addHorizontalRule(XWPFDocument doc, boolean centered) {
        XWPFParagraph hr = doc.createParagraph();
        if (centered) hr.setAlignment(ParagraphAlignment.CENTER);
        hr.setSpacingBefore(80);
        hr.setSpacingAfter(80);
        hr.setBorderBottom(Borders.SINGLE);
    }

    // -------------------------------------------------------------------------
    // Table
    // -------------------------------------------------------------------------

    private void addTable(XWPFDocument doc, Element tableEl) {
        Elements rows = tableEl.select("tr");
        if (rows.isEmpty()) return;

        int colCount = rows.stream().mapToInt(r -> r.select("th, td").size()).max().orElse(1);
        XWPFTable table = doc.createTable(rows.size(), colCount);
        table.setStyleID("TableGrid");
        setTableFullWidth(table);

        for (int r = 0; r < rows.size(); r++) {
            Elements cells = rows.get(r).select("th, td");
            boolean isHeader = !rows.get(r).select("th").isEmpty();
            XWPFTableRow tableRow = table.getRow(r);

            for (int c = 0; c < cells.size(); c++) {
                XWPFTableCell cell = c < tableRow.getTableCells().size()
                        ? tableRow.getCell(c)
                        : tableRow.addNewTableCell();

                if (isHeader) {
                    cell.setColor(COLOR_GREY);
                }

                XWPFParagraph cellPara = cell.getParagraphs().get(0);
                if (isHeader) cellPara.setAlignment(ParagraphAlignment.CENTER);

                XWPFRun run = styleRun(cellPara.createRun(), isHeader, SIZE_BODY);
                run.setText(cells.get(c).text());
            }
        }
    }

    /** Set table to 100% page width using the low-level CT API. */
    private void setTableFullWidth(XWPFTable table) {
        try {
            CTTbl tbl = table.getCTTbl();
            CTTblPr tblPr = tbl.getTblPr();
            if (tblPr == null) tblPr = tbl.addNewTblPr();
            CTTblWidth w = tblPr.getTblW();
            if (w == null) w = tblPr.addNewTblW();
            w.setType(STTblWidth.PCT);
            w.setW(BigInteger.valueOf(5000)); // 100% in twentieths-of-a-percent
        } catch (Exception e) {
            log.debug("setTableFullWidth skipped: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private XWPFRun styleRun(XWPFRun run, boolean bold, int sizePt) {
        run.setFontFamily(FONT);
        run.setFontSize(sizePt);
        if (bold) run.setBold(true);
        return run;
    }

}
