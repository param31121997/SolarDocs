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

/** Undertaking / Self-Declaration for Domestic Content Requirement (DCR) fulfillment. */
@Component
public class DcrDeclarationGenerationStrategy implements DocumentGenerationStrategy {

    private final VendorProfileRepository vendorProfileRepository;

    public DcrDeclarationGenerationStrategy(VendorProfileRepository vendorProfileRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
    }

    @Override
    public String templateCode() { return "DCR_DECLARATION"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        VendorProfile vendor = vendorProfileRepository.find().orElse(null);
        Address a = customer.getAddress() != null ? customer.getAddress() : new Address("", "", "", "", "");

        Map<String, Object> model = new HashMap<>();
        // --- Already on Customer Details ---
        model.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.put("consumerName", customer.getName());
        model.put("consumerAddress", formatAddress(a));
        model.put("applicationNumber", customer.getApplicationNumber() != null ? customer.getApplicationNumber() : "");
        model.put("discom", customer.getDiscom() != null ? customer.getDiscom() : "");
        model.put("plantCapacityKw", customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : "");

        // --- Vendor identity: defaults from Settings > Vendor Profile, overridable ---
        model.put("vendorCompanyName", extraFields.getOrDefault("vendorCompanyName",
                vendor != null && vendor.companyName() != null ? vendor.companyName() : ""));
        model.put("vendorSignatoryName", extraFields.getOrDefault("vendorSignatoryName",
                vendor != null && vendor.signatoryName() != null ? vendor.signatoryName() : ""));
        model.put("vendorPhone", extraFields.getOrDefault("vendorPhone",
                vendor != null && vendor.phone() != null ? vendor.phone() : ""));
        model.put("vendorEmail", extraFields.getOrDefault("vendorEmail",
                vendor != null && vendor.email() != null ? vendor.email() : ""));

        // --- Not tracked anywhere yet: collected on the Generate Document screen ---
        model.put("installationDate", extraFields.getOrDefault("installationDate", ""));
        model.put("moduleCapacityKwp", extraFields.getOrDefault("moduleCapacityKwp",
                customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : ""));
        model.put("moduleCount", extraFields.getOrDefault("moduleCount", ""));
        model.put("moduleSerialNumbers", extraFields.getOrDefault("moduleSerialNumbers", ""));
        model.put("cellManufacturerName", extraFields.getOrDefault("cellManufacturerName", ""));
        model.put("cellGstInvoiceNo", extraFields.getOrDefault("cellGstInvoiceNo", ""));

        // --- Yellow: default + dynamic ---
        model.put("moduleMake", extraFields.getOrDefault("moduleMake", "Adani"));

        return model;
    }

    private static String formatAddress(Address a) {
        return String.join(", ", java.util.stream.Stream.of(a.addressLine(), a.village(), a.district(), a.pincode())
                .filter(s -> s != null && !s.isBlank()).toList());
    }
}
