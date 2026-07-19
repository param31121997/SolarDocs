package com.solardocs.infrastructure.persistence.json;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CustomerJsonRecord(
        String customerId,
        String name,
        String mobile,
        String alternateMobile,
        String addressLine,
        String village,
        String district,
        String state,
        String pincode,
        String consumerNumber,
        String applicationNumber,
        BigDecimal sanctionedLoadKw,
        BigDecimal plantCapacityKw,
        String discom,
        String category,
        String status,
        boolean archived,
        List<UploadedDocRecord> uploadedDocuments,
        List<GeneratedDocRecord> generatedDocuments,
        Instant createdAt,
        Instant updatedAt
) {
    public record UploadedDocRecord(String id, String type, String fileName, String filePath, Instant uploadedAt) {}
    public record GeneratedDocRecord(String id, String templateCode, String templateVersion, String filePath, Instant generatedAt) {}
}
