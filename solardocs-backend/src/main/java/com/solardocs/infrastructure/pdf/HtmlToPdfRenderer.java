package com.solardocs.infrastructure.pdf;

import com.solardocs.config.AppDataDirectoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
public class HtmlToPdfRenderer implements PdfRenderer {

    private static final Logger log = LoggerFactory.getLogger(HtmlToPdfRenderer.class);
    private final TemplateEngine templateEngine;
    private final Path templatesDir;

    public HtmlToPdfRenderer(AppDataDirectoryConfig dirs) {
        this.templatesDir = dirs.templatesDir();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(templatesDir.toString() + java.io.File.separator);
        resolver.setSuffix("");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
        log.info("PDF Renderer initialized with templates dir: {}", templatesDir);
    }

    @Override
    public byte[] render(String templateName, Map<String, Object> model) {
        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }

        Path templatePath = templatesDir.resolve(templateName);
        if (Files.notExists(templatePath)) {
            log.error("Template file not found at: {}", templatePath);
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        try {
            log.debug("Processing template: {}", templateName);
            Context context = new Context();
            context.setVariables(model);
            String html = templateEngine.process(templateName, context);

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                var builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
                log.info("PDF generated successfully for template: {}", templateName);
                return os.toByteArray();
            }
        } catch (Exception e) {
            log.error("PDF rendering failed for template: {}", templateName, e);
            throw new RuntimeException("PDF rendering failed: " + e.getMessage(), e);
        }
    }
}
