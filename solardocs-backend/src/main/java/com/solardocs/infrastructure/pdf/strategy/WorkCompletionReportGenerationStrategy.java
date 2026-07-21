package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.application.ports.VendorProfileRepository;
import com.solardocs.domain.common.Address;
import com.solardocs.domain.customer.Customer;
import com.solardocs.domain.vendor.VendorProfile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * "Work Completion Report for Solar Power Plant" - the vendor+consumer
 * signed handover certificate. Consumer/site fields come from the
 * Customer record (already captured on Customer Details); everything
 * about the installed equipment is plant-specific and not something the
 * Customer record tracks, so it is supplied per-generation via
 * extraFields on the Generate Document screen. A handful of fields
 * (module make, ALMM number, year of manufacture, etc.) are vendor
 * defaults that rarely change between customers, so they carry a
 * sensible default but stay overridable.
 */
@Component
public class WorkCompletionReportGenerationStrategy implements DocumentGenerationStrategy {

    private final VendorProfileRepository vendorProfileRepository;

    public WorkCompletionReportGenerationStrategy(VendorProfileRepository vendorProfileRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
    }

    @Override
    public String templateCode() { return "WORK_COMPLETION_REPORT"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        VendorProfile vendor = vendorProfileRepository.find().orElse(null);
        Address a = customer.getAddress() != null ? customer.getAddress() : new Address("", "", "", "", "");

        Map<String, Object> model = new HashMap<>();
        // --- Consumer / site fields: already on Customer Details ---
        model.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.put("consumerName", customer.getName());
        model.put("consumerNumber", customer.getConsumerNumber() != null ? customer.getConsumerNumber() : "");
        model.put("siteAddress", formatAddress(a));
        model.put("district", a.district() != null ? a.district() : "");
        model.put("state", a.state() != null ? a.state() : "");
        model.put("pincode", a.pincode() != null ? a.pincode() : "");
        model.put("sanctionedCapacityKw", customer.getSanctionedLoadKw() != null ? customer.getSanctionedLoadKw().toString() : "");
        model.put("installedCapacityKw", customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : "");

        // --- Vendor identity: defaults from Settings > Vendor Profile, overridable ---
        model.put("vendorCompanyName", extraFields.getOrDefault("vendorCompanyName",
                vendor != null && vendor.companyName() != null ? vendor.companyName() : ""));

        // --- Fields not tracked on Customer: collected on the Generate Document screen ---
        model.put("category", extraFields.getOrDefault("category", "Private"));               // yellow, default
        model.put("sanctionNumber", extraFields.getOrDefault("sanctionNumber",
                customer.getApplicationNumber() != null ? customer.getApplicationNumber() : ""));
        model.put("installationDate", extraFields.getOrDefault("installationDate", ""));

        model.put("moduleMake", extraFields.getOrDefault("moduleMake", "Mundra Solar PV Limited (Adani)")); // yellow
        model.put("almmModelNumber", extraFields.getOrDefault("almmModelNumber", "ASB-M10-144"));            // yellow
        model.put("moduleWattage", extraFields.getOrDefault("moduleWattage", ""));
        model.put("moduleCount", extraFields.getOrDefault("moduleCount", ""));
        model.put("moduleTotalCapacityKwp", extraFields.getOrDefault("moduleTotalCapacityKwp",
                customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : ""));
        model.put("warrantyDetails", extraFields.getOrDefault("warrantyDetails", "25 years"));               // yellow

        model.put("inverterMake", extraFields.getOrDefault("inverterMake", ""));
        model.put("inverterRating", extraFields.getOrDefault("inverterRating", ""));
        model.put("chargeControllerType", extraFields.getOrDefault("chargeControllerType", ""));
        model.put("inverterCapacityKw", extraFields.getOrDefault("inverterCapacityKw", ""));
        model.put("hpd", extraFields.getOrDefault("hpd", ""));
        model.put("yearOfManufacturing", extraFields.getOrDefault("yearOfManufacturing",
                String.valueOf(LocalDate.now().getYear())));                                                 // yellow

        model.put("earthing1Ohms", extraFields.getOrDefault("earthing1Ohms", ""));
        model.put("earthing2Ohms", extraFields.getOrDefault("earthing2Ohms", ""));
        model.put("earthing3Ohms", extraFields.getOrDefault("earthing3Ohms", ""));
        model.put("earthResistanceCertified", extraFields.getOrDefault("earthResistanceCertified", "Yes"));  // yellow
        model.put("lightningArrester", extraFields.getOrDefault("lightningArrester", "Yes"));                // yellow

        return model;
    }

    private static String formatAddress(Address a) {
        return String.join(", ", java.util.stream.Stream.of(a.addressLine(), a.village())
                .filter(s -> s != null && !s.isBlank()).toList());
    }
}
