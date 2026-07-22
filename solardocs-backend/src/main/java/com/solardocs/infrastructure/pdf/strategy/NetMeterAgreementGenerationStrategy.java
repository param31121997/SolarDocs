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

/** Net Metering Connection Agreement between the Eligible Consumer and the Distribution Licensee, witnessed by the vendor. */
@Component
public class NetMeterAgreementGenerationStrategy implements DocumentGenerationStrategy {

    private final VendorProfileRepository vendorProfileRepository;

    public NetMeterAgreementGenerationStrategy(VendorProfileRepository vendorProfileRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
    }

    @Override
    public String templateCode() { return "NET_METER_AGREEMENT"; }

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
        model.put("consumerAddress", formatAddress(a));

        model.put("vendorWitnessName", extraFields.getOrDefault("vendorWitnessName",
                vendor != null && vendor.companyName() != null ? vendor.companyName() : ""));
        var pd = customer.getPlantDetails();
        model.put("agreementPlace", FieldResolver.resolve(extraFields, "agreementPlace", pd,
                com.solardocs.domain.customer.PlantInstallationDetails::agreementPlace, ""));
        model.put("netMeterSerialNo", FieldResolver.resolve(extraFields, "netMeterSerialNo", pd,
                com.solardocs.domain.customer.PlantInstallationDetails::netMeterSerialNo, ""));

        return model;
    }

    private static String formatAddress(Address a) {
        return String.join(", ", java.util.stream.Stream.of(a.addressLine(), a.village(), a.district(), a.pincode())
                .filter(s -> s != null && !s.isBlank()).toList());
    }
}
