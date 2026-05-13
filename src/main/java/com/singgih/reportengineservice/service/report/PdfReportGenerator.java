package com.singgih.reportengineservice.service.report;

import com.lowagie.text.DocumentException;
import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReportGenerator implements ReportGeneratorStrategy {

    private final TemplateProcessingService templateProcessor;

    @Override
    public ReportType getSupportedType() {
        return ReportType.PDF;
    }

    @Override
    public byte[] generate(String template, Map<String, Object> params) {
        String html  = templateProcessor.processHtml(template, params);
        String xhtml = toXhtml(html);
        return renderPdf(xhtml);
    }

    /** Flying Saucer requires well-formed XHTML — Jsoup normalises the HTML output. */
    private String toXhtml(String html) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .charset(StandardCharsets.UTF_8);
        return doc.html();
    }

    private byte[] renderPdf(String xhtml) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new ReportGenerationException("Gagal membuat PDF", e);
        }
    }
}
