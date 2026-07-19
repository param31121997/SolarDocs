package com.solardocs.application.document;

import com.solardocs.application.customer.CustomerService;
import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.document.UploadedDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Service
public class DocumentUploadService {

    private final CustomerService customerService;
    private final AppDataDirectoryConfig dirs;

    public DocumentUploadService(CustomerService customerService, AppDataDirectoryConfig dirs) {
        this.customerService = customerService;
        this.dirs = dirs;
    }

    public UploadedDocument upload(String customerId, String docType, MultipartFile file) {
        if (docType == null || !docType.matches("[A-Z][A-Z0-9_]{0,63}")) {
            throw new IllegalArgumentException("Invalid document type");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A non-empty file is required");
        }

        customerService.get(customerId);
        try {
            Path folder = Files.list(dirs.customersDir())
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith(customerId + "-"))
                    .findFirst()
                    .orElseThrow();

            boolean isPhoto = docType.startsWith("SITE_PHOTO") || docType.equals("CUSTOMER_SIGNATURE");
            Path targetDir = folder.resolve(isPhoto ? "Images" : "Documents");
            Files.createDirectories(targetDir);

            String id = UUID.randomUUID().toString();
            String originalFileName = safeFileName(file.getOriginalFilename());
            String safeName = docType + "_" + id.substring(0, 8) + "_" + originalFileName;
            Path target = targetDir.resolve(safeName);
            file.transferTo(target);

            UploadedDocument doc = new UploadedDocument(id, docType, originalFileName,
                    target.toString(), Instant.now());
            customerService.addUploadedDocument(customerId, doc);
            return doc;
        } catch (IOException e) {
            throw new RuntimeException("Upload failed for customer " + customerId, e);
        }
    }

    private String safeFileName(String originalFileName) {
        String name = originalFileName == null ? "upload" : originalFileName.replace('\\', '/');
        int lastSeparator = name.lastIndexOf('/');
        if (lastSeparator >= 0) {
            name = name.substring(lastSeparator + 1);
        }
        String sanitized = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        if (sanitized.isBlank() || sanitized.equals(".") || sanitized.equals("..")) {
            return "upload";
        }
        return sanitized.length() > 120 ? sanitized.substring(0, 120) : sanitized;
    }
}
