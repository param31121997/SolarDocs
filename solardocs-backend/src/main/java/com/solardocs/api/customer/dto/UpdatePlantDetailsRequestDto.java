package com.solardocs.api.customer.dto;

/**
 * All optional - the vendor fills in whatever is known at the time
 * (often incrementally, as installation/commissioning progresses).
 * Every field here maps 1:1 to PlantInstallationDetails and is reused
 * by every compliance document strategy instead of being asked again
 * per document.
 */
public record UpdatePlantDetailsRequestDto(
        String email,
        String installationDate,
        String inverterMake,
        String inverterRating,
        String inverterCapacityKw,
        String chargeControllerType,
        String hpd,
        String earthing1Ohms,
        String earthing2Ohms,
        String earthing3Ohms,
        String moduleWattage,
        String moduleCount,
        String moduleCapacityKw,
        String moduleSerialNumbers,
        String cellManufacturerName,
        String cellGstInvoiceNo,
        String aadhaarNumber,
        String inspectionDate,
        String inspectionLetterNo,
        String inspectionLetterDate,
        String agreementPlace,
        String netMeterSerialNo
) {}
