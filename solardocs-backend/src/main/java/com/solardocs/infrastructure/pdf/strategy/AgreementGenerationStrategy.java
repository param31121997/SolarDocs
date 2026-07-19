package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.domain.customer.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class AgreementGenerationStrategy implements DocumentGenerationStrategy {

    @Override
    public String templateCode() { return "AGREEMENT"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        var a = customer.getAddress();
        String consumerAddress = String.join(", ",
                java.util.stream.Stream.of(a != null ? a.addressLine() : "", a != null ? a.village() : "",
                        a != null ? a.district() : "", a != null ? a.pincode() : "")
                        .filter(s -> s != null && !s.isBlank()).toList());

        return Map.of(
                "agreementDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "consumerName", customer.getName(),
                "consumerAddress", consumerAddress,
                "vendorCompanyName", extraFields.getOrDefault("vendorCompanyName", ""),
                "vendorRegisteredAddress", extraFields.getOrDefault("vendorRegisteredAddress", ""),
                "plantCapacityKw", customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : ""
        );
    }
}
