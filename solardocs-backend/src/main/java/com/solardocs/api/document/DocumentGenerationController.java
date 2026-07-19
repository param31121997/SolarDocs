package com.solardocs.api.document;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.application.document.DocumentGenerationService;
import com.solardocs.application.template.TemplateManagementService;
import com.solardocs.domain.document.GeneratedDocument;
import com.solardocs.domain.template.DocumentTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DocumentGenerationController {

    private final DocumentGenerationService generationService;
    private final TemplateManagementService templateService;

    public DocumentGenerationController(DocumentGenerationService generationService, TemplateManagementService templateService) {
        this.generationService = generationService;
        this.templateService = templateService;
    }

    @GetMapping("/templates")
    public ApiResponse<List<DocumentTemplate>> templates() {
        return ApiResponse.ok(templateService.listActive());
    }

    public record GenerateRequest(String templateCode, Map<String, Object> extraFields) {}

    @PostMapping("/customers/{id}/documents/generate")
    public ApiResponse<GeneratedDocument> generate(@PathVariable String id, @RequestBody GenerateRequest req) {
        try {
            if (id == null || id.isBlank()) {
                return ApiResponse.fail("INVALID_CUSTOMER_ID", "Customer ID is required");
            }
            if (req.templateCode() == null || req.templateCode().isBlank()) {
                return ApiResponse.fail("INVALID_TEMPLATE", "Template code is required");
            }
            GeneratedDocument doc = generationService.generate(id, req.templateCode(), req.extraFields());
            return ApiResponse.ok(doc);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail("INVALID_REQUEST", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("PDF_GENERATION_ERROR", "Failed to generate document: " + e.getMessage());
        }
    }
}
