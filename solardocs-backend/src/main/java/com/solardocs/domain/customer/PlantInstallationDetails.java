package com.solardocs.domain.customer;

/**
 * Installation-specific technical facts that the compliance document set
 * (Work Completion Report, Guarantee Certificate, Annexure-I, Proforma-A,
 * DCR Declaration, Net Meter Agreement) needs, but that don't belong on
 * the core Customer record because they're facts about the physical
 * plant and paperwork trail, not the consumer's identity. Captured once
 * on the Plant Details tab, then reused by every document strategy -
 * nothing here is re-asked per document.
 * <p>
 * Consumer identity fields (email, Aadhaar number) live directly on
 * Customer alongside name/mobile - see Customer.java - since they're
 * facts about the person, not the installation.
 * <p>
 * Every field is a plain String kept exactly as the vendor enters it
 * (dates as free text, e.g. "14/03/2026") since these are dropped
 * straight into HTML templates with no further computation.
 */
public record PlantInstallationDetails(
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
        String inspectionDate,
        String inspectionLetterNo,
        String inspectionLetterDate,
        String agreementPlace,
        String netMeterSerialNo
) {
    public static PlantInstallationDetails empty() {
        return new PlantInstallationDetails(
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
        );
    }

    /** Null-safe accessor - rehydrated customers from before this field existed have a null record. */
    public static PlantInstallationDetails orEmpty(PlantInstallationDetails details) {
        return details == null ? empty() : details;
    }
}
