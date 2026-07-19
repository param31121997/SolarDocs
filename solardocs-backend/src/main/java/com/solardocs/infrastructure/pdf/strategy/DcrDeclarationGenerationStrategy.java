package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.domain.customer.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class DcrDeclarationGenerationStrategy implements DocumentGenerationStrategy {

    @Override
    public String templateCode() { return "DCR_DECLARATION"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        return Map.of(
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "consumerName", customer.getName(),
                "applicationNumber", customer.getApplicationNumber() != null ? customer.getApplicationNumber() : "",
                "plantCapacityKw", customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : "",
                "moduleMake", extraFields.getOrDefault("moduleMake", ""),
                "moduleModel", extraFields.getOrDefault("moduleModel", ""),
                "vendorCompanyName", extraFields.getOrDefault("vendorCompanyName", ""),
                "almmListingRef", extraFields.getOrDefault("almmListingRef", "")
        );
    }
}
