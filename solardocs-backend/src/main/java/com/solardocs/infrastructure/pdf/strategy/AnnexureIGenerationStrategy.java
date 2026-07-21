package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.domain.common.Address;
import com.solardocs.domain.customer.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class AnnexureIGenerationStrategy implements DocumentGenerationStrategy {

    @Override
    public String templateCode() { return "ANNEXURE_I"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        Address a = customer.getAddress() != null ? customer.getAddress() : new Address("", "", "", "", "");

        Map<String, Object> model = new HashMap<>();
        // --- Already on Customer Details ---
        model.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.put("consumerName", customer.getName());
        model.put("consumerNumber", customer.getConsumerNumber() != null ? customer.getConsumerNumber() : "");
        model.put("mobile", customer.getMobile() != null ? customer.getMobile() : "");
        model.put("applicationNumber", customer.getApplicationNumber() != null ? customer.getApplicationNumber() : "");
        model.put("discom", customer.getDiscom() != null ? customer.getDiscom() : "");
        model.put("addressOfInstallation", formatAddress(a));
        model.put("district", a.district() != null ? a.district() : "");
        model.put("sanctionedCapacityKw", customer.getSanctionedLoadKw() != null ? customer.getSanctionedLoadKw().toString() : "");
        model.put("plantCapacityKw", customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : "");
        model.put("reInstalledCapacityRooftopKw", customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : "");

        // --- Not on Customer: collected on the Generate Document screen ---
        model.put("email", extraFields.getOrDefault("email", ""));
        model.put("installationDate", extraFields.getOrDefault("installationDate", ""));
        model.put("inverterCapacityKw", extraFields.getOrDefault("inverterCapacityKw", ""));
        model.put("inverterMake", extraFields.getOrDefault("inverterMake", ""));
        model.put("moduleCount", extraFields.getOrDefault("moduleCount", ""));
        model.put("moduleCapacityKw", extraFields.getOrDefault("moduleCapacityKw", ""));
        model.put("reInstalledCapacityRooftopGroundKw", extraFields.getOrDefault("reInstalledCapacityRooftopGroundKw", "NA"));
        model.put("reInstalledCapacityGroundKw", extraFields.getOrDefault("reInstalledCapacityGroundKw", "NA"));

        // --- Yellow: default + dynamic ---
        model.put("reArrangementType", extraFields.getOrDefault("reArrangementType", "Net Metering Arrangement"));
        model.put("reSource", extraFields.getOrDefault("reSource", "Solar"));
        model.put("capacityType", extraFields.getOrDefault("capacityType", "Rooftop"));
        model.put("projectModel", extraFields.getOrDefault("projectModel", "capex"));

        return model;
    }

    private static String formatAddress(Address a) {
        return String.join(", ", java.util.stream.Stream.of(a.addressLine(), a.village())
                .filter(s -> s != null && !s.isBlank()).toList());
    }
}
