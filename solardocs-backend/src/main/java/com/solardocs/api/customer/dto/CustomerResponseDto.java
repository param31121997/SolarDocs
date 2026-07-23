package com.solardocs.api.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CustomerResponseDto(
        String customerId, String name, String mobile, String alternateMobile,
        String email, String aadhaarNumber,
        String addressLine, String village, String district, String state, String pincode,
        String consumerNumber, String applicationNumber,
        BigDecimal sanctionedLoadKw, BigDecimal plantCapacityKw,
        String discom, String category, String status,
        String plantInstallationDate, String plantInverterMake, String plantInverterRating,
        String plantInverterCapacityKw, String plantChargeControllerType, String plantHpd,
        String plantEarthing1Ohms, String plantEarthing2Ohms, String plantEarthing3Ohms,
        String plantModuleWattage, String plantModuleCount, String plantModuleCapacityKw, String plantModuleSerialNumbers,
        String plantCellManufacturerName, String plantCellGstInvoiceNo,
        String plantInspectionDate, String plantInspectionLetterNo, String plantInspectionLetterDate,
        String plantAgreementPlace, String plantNetMeterSerialNo,
        Instant createdAt, Instant updatedAt
) {
}
