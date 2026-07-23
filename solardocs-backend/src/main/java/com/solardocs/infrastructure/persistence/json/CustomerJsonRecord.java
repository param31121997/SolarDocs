package com.solardocs.infrastructure.persistence.json;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CustomerJsonRecord(
        String customerId,
        String name,
        String mobile,
        String alternateMobile,
        String email,
        String aadhaarNumber,
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
        PlantDetailsRecord plantDetails,
        Instant createdAt,
        Instant updatedAt
) {
    public record UploadedDocRecord(String id, String type, String fileName, String filePath, Instant uploadedAt) {}
    public record GeneratedDocRecord(String id, String templateCode, String templateVersion, String filePath, Instant generatedAt) {}

    /**
     * Mirrors domain PlantInstallationDetails - kept as a separate JSON-layer
     * record (rather than reusing the domain type directly) so the
     * persistence format doesn't change if the domain shape ever does.
     * email/aadhaarNumber moved out to the top-level Customer fields above -
     * they're consumer identity, not plant/installation facts.
     */
    public record PlantDetailsRecord(
            String installationDate, String inverterMake, String inverterRating,
            String inverterCapacityKw, String chargeControllerType, String hpd,
            String earthing1Ohms, String earthing2Ohms, String earthing3Ohms,
            String moduleWattage, String moduleCount, String moduleCapacityKw, String moduleSerialNumbers,
            String cellManufacturerName, String cellGstInvoiceNo,
            String inspectionDate, String inspectionLetterNo, String inspectionLetterDate,
            String agreementPlace, String netMeterSerialNo
    ) {}
}
