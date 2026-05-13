package com.singgih.reportengineservice.service.report;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

/**
 * Wraps two dedicated SpringTemplateEngine instances (HTML and TEXT mode) for
 * processing templates stored in the database as strings.
 *
 * SpringTemplateEngine uses SpringEL — no OGNL dependency required.
 * Engines are created programmatically (not as Spring beans) to avoid
 * triggering ThymeleafAutoConfiguration's @ConditionalOnMissingBean(ISpringTemplateEngine).
 */
@Service
public class TemplateProcessingService {

    private final SpringTemplateEngine htmlEngine;
    private final SpringTemplateEngine textEngine;

    public TemplateProcessingService() {
        htmlEngine = buildEngine(TemplateMode.HTML);
        textEngine = buildEngine(TemplateMode.TEXT);
    }

    public String processHtml(String template, Map<String, Object> vars) {
        return process(htmlEngine, template, vars);
    }

    public String processText(String template, Map<String, Object> vars) {
        return process(textEngine, template, vars);
    }

    private String process(SpringTemplateEngine engine, String template, Map<String, Object> vars) {
        Context ctx = new Context();
        ctx.setVariables(vars);
        return engine.process(template, ctx);
    }

    private static SpringTemplateEngine buildEngine(TemplateMode mode) {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(mode);
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(resolver);
        return engine;
    }
}
