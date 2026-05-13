package com.singgih.reportengineservice.service.report;

import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Renders a Thymeleaf TEXT-mode template and returns the result as UTF-8 bytes.
 * The template is responsible for producing valid CSV content.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvReportGenerator implements ReportGeneratorStrategy {

    private final TemplateProcessingService templateProcessor;

    @Override
    public ReportType getSupportedType() {
        return ReportType.CSV;
    }

    @Override
    public byte[] generate(String template, Map<String, Object> params) {
        try {
            String csv = templateProcessor.processText(template, params);
            return csv.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ReportGenerationException("Gagal membuat CSV", e);
        }
    }
}
