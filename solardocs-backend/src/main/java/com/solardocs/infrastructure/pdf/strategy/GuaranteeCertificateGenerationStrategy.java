package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.application.ports.VendorProfileRepository;
import com.solardocs.domain.customer.Customer;
import com.solardocs.domain.vendor.VendorProfile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** "Guarantee Certificate Undertaking to be submitted by VENDOR" + consumer identity (Aadhaar). */
@Component
public class GuaranteeCertificateGenerationStrategy implements DocumentGenerationStrategy {

    private final VendorProfileRepository vendorProfileRepository;

    public GuaranteeCertificateGenerationStrategy(VendorProfileRepository vendorProfileRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
    }

    @Override
    public String templateCode() { return "GUARANTEE_CERTIFICATE"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        VendorProfile vendor = vendorProfileRepository.find().orElse(null);

        return Map.of(
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "consumerName", customer.getName(),
                "consumerNumber", customer.getConsumerNumber() != null ? customer.getConsumerNumber() : "",
                "vendorCompanyName", extraFields.getOrDefault("vendorCompanyName",
                        vendor != null && vendor.companyName() != null ? vendor.companyName() : ""),
                "cmcYears", extraFields.getOrDefault("cmcYears", "5"),                 // yellow, default
                // Aadhaar now lives on Customer > Plant Details, filled once and
                // reused here - extraFields can still override it per-generation.
                "aadhaarNumber", FieldResolver.resolve(extraFields, "aadhaarNumber", customer.getPlantDetails(),
                        com.solardocs.domain.customer.PlantInstallationDetails::aadhaarNumber, "")
        );
    }
}
