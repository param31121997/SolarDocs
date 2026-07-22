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

/** Proforma-A - Commissioning Report (Provisional) for Grid Connected Solar PV Power Plant. */
@Component
public class CommissioningReportGenerationStrategy implements DocumentGenerationStrategy {

    private final VendorProfileRepository vendorProfileRepository;

    public CommissioningReportGenerationStrategy(VendorProfileRepository vendorProfileRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
    }

    @Override
    public String templateCode() { return "COMMISSIONING_REPORT"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        VendorProfile vendor = vendorProfileRepository.find().orElse(null);
        Address a = customer.getAddress() != null ? customer.getAddress() : new Address("", "", "", "", "");

        Map<String, Object> model = new HashMap<>();
        model.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.put("consumerName", customer.getName());
        model.put("consumerNumber", customer.getConsumerNumber() != null ? customer.getConsumerNumber() : "");
        model.put("applicationNumber", customer.getApplicationNumber() != null ? customer.getApplicationNumber() : "");
        model.put("discom", customer.getDiscom() != null ? customer.getDiscom() : "");
        model.put("plantCapacityKw", customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : "");
        model.put("siteAddress", formatAddress(a));

        model.put("vendorCompanyName", extraFields.getOrDefault("vendorCompanyName",
                vendor != null && vendor.companyName() != null ? vendor.companyName() : ""));
        var pd = customer.getPlantDetails();
        model.put("installationDate", FieldResolver.resolve(extraFields, "installationDate", pd,
                com.solardocs.domain.customer.PlantInstallationDetails::installationDate, ""));
        model.put("inspectionDate", FieldResolver.resolve(extraFields, "inspectionDate", pd,
                com.solardocs.domain.customer.PlantInstallationDetails::inspectionDate, ""));
        model.put("inspectionLetterNo", FieldResolver.resolve(extraFields, "inspectionLetterNo", pd,
                com.solardocs.domain.customer.PlantInstallationDetails::inspectionLetterNo, ""));
        model.put("inspectionLetterDate", FieldResolver.resolve(extraFields, "inspectionLetterDate", pd,
                com.solardocs.domain.customer.PlantInstallationDetails::inspectionLetterDate, ""));

        return model;
    }

    private static String formatAddress(Address a) {
        return String.join(", ", java.util.stream.Stream.of(a.addressLine(), a.village(), a.district(), a.pincode())
                .filter(s -> s != null && !s.isBlank()).toList());
    }
}
