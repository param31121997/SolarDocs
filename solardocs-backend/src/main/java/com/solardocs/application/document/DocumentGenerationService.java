package com.solardocs.application.document;

import com.solardocs.application.customer.CustomerService;
import com.solardocs.application.template.TemplateManagementService;
import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.customer.Customer;
import com.solardocs.domain.document.GeneratedDocument;
import com.solardocs.domain.template.DocumentTemplate;
import com.solardocs.infrastructure.pdf.DocumentGenerationStrategyFactory;
import com.solardocs.infrastructure.pdf.PdfRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentGenerationService {

    private static final Logger log = LoggerFactory.getLogger(DocumentGenerationService.class);

    private final CustomerService customerService;
    private final TemplateManagementService templateService;
    private final DocumentGenerationStrategyFactory strategyFactory;
    private final PdfRenderer pdfRenderer;
    private final AppDataDirectoryConfig dirs;

    public DocumentGenerationService(CustomerService customerService, TemplateManagementService templateService,
                                      DocumentGenerationStrategyFactory strategyFactory, PdfRenderer pdfRenderer,
                                      AppDataDirectoryConfig dirs) {
        this.customerService = customerService;
        this.templateService = templateService;
        this.strategyFactory = strategyFactory;
        this.pdfRenderer = pdfRenderer;
        this.dirs = dirs;
    }

    public GeneratedDocument generate(String customerId, String templateCode, Map<String, Object> extraFields) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        if (templateCode == null || templateCode.isBlank()) {
            throw new IllegalArgumentException("Template code cannot be null or empty");
        }

        try {
            log.info("Starting document generation for customer: {} with template: {}", customerId, templateCode);

            // Get customer
            Customer customer = customerService.get(customerId);
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found: " + customerId);
            }

            // Get template
            DocumentTemplate template = templateService.getByCode(templateCode);
            if (template == null) {
                throw new IllegalArgumentException("Template not found: " + templateCode);
            }

            // Get generation strategy
            var strategy = strategyFactory.resolve(templateCode);
            if (strategy == null) {
                throw new IllegalArgumentException("No generation strategy found for template: " + templateCode);
            }

            // Build model and render PDF
            Map<String, Object> model = strategy.buildModel(customer, extraFields == null ? Map.of() : extraFields);
            byte[] pdfBytes = pdfRenderer.render(template.htmlFile(), model);

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new RuntimeException("PDF rendering returned empty bytes for template: " + templateCode);
            }

            log.debug("PDF rendered successfully, size: {} bytes", pdfBytes.length);

            // Find customer folder
            Path customerFolder = findCustomerFolder(customerId);
            if (customerFolder == null) {
                throw new RuntimeException("Customer folder not found for: " + customerId);
            }

            // Create GeneratedPDF directory
            Path pdfDir = customerFolder.resolve("GeneratedPDF");
            Files.createDirectories(pdfDir);
            log.debug("PDF directory ready at: {}", pdfDir);

            // Generate unique filename and save PDF
            String docId = UUID.randomUUID().toString();
            String fileName = templateCode + "_" + template.version() + "_" + System.currentTimeMillis() + ".pdf";
            Path targetFile = pdfDir.resolve(fileName);
            Files.write(targetFile, pdfBytes);
            log.info("PDF saved to: {}", targetFile);

            // Create GeneratedDocument record
            GeneratedDocument doc = new GeneratedDocument(docId, templateCode, template.version(), targetFile.toString(), Instant.now());

            // Add to customer and persist
            customerService.addGeneratedDocument(customerId, doc);
            log.info("Document generation complete and saved for customer: {} with template: {}", customerId, templateCode);

            return doc;
        } catch (IllegalArgumentException e) {
            log.error("Validation error during document generation: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("IO error during document generation for customer: {} template: {}", customerId, templateCode, e);
            throw new RuntimeException("Failed to save PDF file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error generating {} for {}: {}", templateCode, customerId, e.getMessage(), e);
            throw new RuntimeException("Failed generating " + templateCode + " for " + customerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Find the customer folder by ID prefix
     * Customers are stored in folders like "A00001-Customer-Name"
     */
    private Path findCustomerFolder(String customerId) {
        try {
            return Files.list(dirs.customersDir())
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith(customerId + "-"))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            log.error("Error listing customer directories", e);
            return null;
        }
    }
}
