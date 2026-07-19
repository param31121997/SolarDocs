package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.domain.customer.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class AnnexureIGenerationStrategy implements DocumentGenerationStrategy {

    @Override
    public String templateCode() { return "ANNEXURE_I"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        return Map.of(
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "consumerName", customer.getName(),
                "consumerNumber", customer.getConsumerNumber() != null ? customer.getConsumerNumber() : "",
                "applicationNumber", customer.getApplicationNumber() != null ? customer.getApplicationNumber() : "",
                "discom", customer.getDiscom() != null ? customer.getDiscom() : "",
                "plantCapacityKw", customer.getPlantCapacityKw() != null ? customer.getPlantCapacityKw().toString() : ""
        );
    }
}
