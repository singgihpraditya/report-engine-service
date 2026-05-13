package com.singgih.reportengineservice.service.report;

import com.singgih.reportengineservice.dto.report.ReportType;

import java.util.Map;

/**
 * Strategy contract for each report format generator.
 * Each implementation handles one ReportType and produces raw file bytes.
 */
public interface ReportGeneratorStrategy {

    ReportType getSupportedType();

    /**
     * @param template fully-assembled Thymeleaf template string (HTML or TEXT)
     * @param params   variables from the report request
     * @return raw bytes of the generated file
     */
    byte[] generate(String template, Map<String, Object> params);
}
