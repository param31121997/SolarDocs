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

            // Generating again replaces the previous copy of this document -
            // no piling up of duplicate PDFs for the same template.
            customerService.replaceGeneratedDocuments(customerId, java.util.Set.of(templateCode));

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
     * The document set called out in the vendor's compliance guidelines
     * (Work Completion Report, Guarantee Certificate + Aadhaar, Annexure-I,
     * Proforma-A / Commissioning Report, DCR Declaration, Net Meter
     * Agreement), in the order they appear there. Brochure/datasheet and
     * site photographs are uploaded files, not generated from a template,
     * so they aren't part of this list - see the Documents tab for those.
     */
    public static final java.util.List<String> COMPLIANCE_PACKAGE_TEMPLATE_CODES = java.util.List.of(
            "WORK_COMPLETION_REPORT", "GUARANTEE_CERTIFICATE", "ANNEXURE_I",
            "COMMISSIONING_REPORT", "DCR_DECLARATION", "NET_METER_AGREEMENT"
    );

    /**
     * Generates every document in COMPLIANCE_PACKAGE_TEMPLATE_CODES for a
     * customer and merges them into one PDF, in a single call - no
     * per-document form. Every field comes from the Customer record
     * (including its Plant Details) and the vendor's Settings profile;
     * each strategy is called with an empty extraFields map, so it always
     * falls through to whatever is saved there (see FieldResolver).
     */
    public GeneratedDocument generatePackage(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        try {
            log.info("Starting compliance package generation for customer: {}", customerId);
            Customer customer = customerService.get(customerId);

            var merger = new org.apache.pdfbox.multipdf.PDFMergerUtility();
            var mergedOut = new java.io.ByteArrayOutputStream();
            merger.setDestinationStream(mergedOut);

            for (String templateCode : COMPLIANCE_PACKAGE_TEMPLATE_CODES) {
                DocumentTemplate template = templateService.getByCode(templateCode);
                var strategy = strategyFactory.resolve(templateCode);
                Map<String, Object> model = strategy.buildModel(customer, Map.of());
                byte[] pdfBytes = pdfRenderer.render(template.htmlFile(), model);
                if (pdfBytes == null || pdfBytes.length == 0) {
                    throw new RuntimeException("PDF rendering returned empty bytes for template: " + templateCode);
                }
                merger.addSource(new org.apache.pdfbox.io.RandomAccessReadBuffer(pdfBytes));
            }
            merger.mergeDocuments(org.apache.pdfbox.io.IOUtils.createMemoryOnlyStreamCache());
            byte[] mergedBytes = mergedOut.toByteArray();

            Path customerFolder = findCustomerFolder(customerId);
            if (customerFolder == null) {
                throw new RuntimeException("Customer folder not found for: " + customerId);
            }
            Path pdfDir = customerFolder.resolve("GeneratedPDF");
            Files.createDirectories(pdfDir);

            String docId = UUID.randomUUID().toString();
            String fileName = "COMPLIANCE_PACKAGE_v1_" + System.currentTimeMillis() + ".pdf";
            Path targetFile = pdfDir.resolve(fileName);
            Files.write(targetFile, mergedBytes);
            log.info("Compliance package saved to: {}", targetFile);

            // Regenerating the package replaces the previous merged PDF -
            // no piling up of duplicate compliance packages.
            customerService.replaceGeneratedDocuments(customerId, java.util.Set.of("COMPLIANCE_PACKAGE"));

            GeneratedDocument doc = new GeneratedDocument(docId, "COMPLIANCE_PACKAGE", "v1", targetFile.toString(), Instant.now());
            customerService.addGeneratedDocument(customerId, doc);
            log.info("Compliance package generation complete for customer: {}", customerId);
            return doc;
        } catch (IllegalArgumentException e) {
            log.error("Validation error during compliance package generation: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("IO error during compliance package generation for customer: {}", customerId, e);
            throw new RuntimeException("Failed to save compliance package: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error generating compliance package for {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed generating compliance package for " + customerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads the bytes for one of this customer's already-generated PDFs,
     * for inline preview/verification in the UI right after generation.
     */
    public byte[] readGeneratedDocument(String customerId, String docId) throws IOException {
        Customer customer = customerService.get(customerId);
        GeneratedDocument doc = customer.getGeneratedDocuments().stream()
                .filter(d -> d.id().equals(docId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Generated document not found: " + docId));
        return Files.readAllBytes(Path.of(doc.filePath()));
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
