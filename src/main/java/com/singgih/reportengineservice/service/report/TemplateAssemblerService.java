package com.singgih.reportengineservice.service.report;

import com.singgih.reportengineservice.entity.ReportTemplate;
import org.springframework.stereotype.Service;

/**
 * Combines a base template (header + footer) with a report body template.
 *
 * Convention: base templates must contain the literal marker {@code {{BODY}}}
 * where the body content should be injected.
 */
@Service
public class TemplateAssemblerService {

    static final String BODY_MARKER = "{{BODY}}";

    /**
     * Returns the assembled template string ready for Thymeleaf processing.
     * If no base template is configured on the report template, the body is returned as-is.
     */
    public String assemble(ReportTemplate reportTemplate) {
        String body = reportTemplate.getTemplate();

        if (reportTemplate.getBaseTemplate() == null) {
            return body;
        }

        String base = reportTemplate.getBaseTemplate().getTemplate();
        if (!base.contains(BODY_MARKER)) {
            return base + body;
        }

        return base.replace(BODY_MARKER, body);
    }
}
