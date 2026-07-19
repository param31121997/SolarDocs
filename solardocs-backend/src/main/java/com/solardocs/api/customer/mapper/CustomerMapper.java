package com.solardocs.api.customer.mapper;

import com.solardocs.api.customer.dto.CustomerResponseDto;
import com.solardocs.domain.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {

    @Mapping(target = "customerId", expression = "java(customer.getId().value())")
    @Mapping(target = "addressLine", source = "address.addressLine")
    @Mapping(target = "village", source = "address.village")
    @Mapping(target = "district", source = "address.district")
    @Mapping(target = "state", source = "address.state")
    @Mapping(target = "pincode", source = "address.pincode")
    @Mapping(target = "status", expression = "java(customer.getStatus().name())")
    CustomerResponseDto toResponse(Customer customer);
}
