package com.solardocs.api.document;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.api.document.dto.UploadedDocumentResponseDto;
import com.solardocs.api.document.mapper.DocumentMapper;
import com.solardocs.application.customer.CustomerService;
import com.solardocs.application.document.DocumentUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/documents")
public class DocumentUploadController {

    private final DocumentUploadService uploadService;
    private final CustomerService customerService;
    private final DocumentMapper documentMapper;

    public DocumentUploadController(DocumentUploadService uploadService, CustomerService customerService,
                                    DocumentMapper documentMapper) {
        this.uploadService = uploadService;
        this.customerService = customerService;
        this.documentMapper = documentMapper;
    }

    @PostMapping
    public ApiResponse<UploadedDocumentResponseDto> upload(@PathVariable String customerId,
                                                            @RequestParam String docType,
                                                            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(documentMapper.toResponse(uploadService.upload(customerId, docType, file)));
    }

    @GetMapping
    public ApiResponse<List<UploadedDocumentResponseDto>> list(@PathVariable String customerId) {
        return ApiResponse.ok(customerService.listUploadedDocuments(customerId).stream()
                .map(documentMapper::toResponse)
                .toList());
    }
}
