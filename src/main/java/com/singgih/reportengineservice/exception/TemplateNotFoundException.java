package com.singgih.reportengineservice.exception;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String templateName, String reportType) {
        super("Template tidak ditemukan: " + templateName + " [" + reportType + "]");
    }
}
