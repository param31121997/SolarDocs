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
    @Mapping(target = "plantEmail", source = "plantDetails.email")
    @Mapping(target = "plantInstallationDate", source = "plantDetails.installationDate")
    @Mapping(target = "plantInverterMake", source = "plantDetails.inverterMake")
    @Mapping(target = "plantInverterRating", source = "plantDetails.inverterRating")
    @Mapping(target = "plantInverterCapacityKw", source = "plantDetails.inverterCapacityKw")
    @Mapping(target = "plantChargeControllerType", source = "plantDetails.chargeControllerType")
    @Mapping(target = "plantHpd", source = "plantDetails.hpd")
    @Mapping(target = "plantEarthing1Ohms", source = "plantDetails.earthing1Ohms")
    @Mapping(target = "plantEarthing2Ohms", source = "plantDetails.earthing2Ohms")
    @Mapping(target = "plantEarthing3Ohms", source = "plantDetails.earthing3Ohms")
    @Mapping(target = "plantModuleWattage", source = "plantDetails.moduleWattage")
    @Mapping(target = "plantModuleCount", source = "plantDetails.moduleCount")
    @Mapping(target = "plantModuleCapacityKw", source = "plantDetails.moduleCapacityKw")
    @Mapping(target = "plantModuleSerialNumbers", source = "plantDetails.moduleSerialNumbers")
    @Mapping(target = "plantCellManufacturerName", source = "plantDetails.cellManufacturerName")
    @Mapping(target = "plantCellGstInvoiceNo", source = "plantDetails.cellGstInvoiceNo")
    @Mapping(target = "plantAadhaarNumber", source = "plantDetails.aadhaarNumber")
    @Mapping(target = "plantInspectionDate", source = "plantDetails.inspectionDate")
    @Mapping(target = "plantInspectionLetterNo", source = "plantDetails.inspectionLetterNo")
    @Mapping(target = "plantInspectionLetterDate", source = "plantDetails.inspectionLetterDate")
    @Mapping(target = "plantAgreementPlace", source = "plantDetails.agreementPlace")
    @Mapping(target = "plantNetMeterSerialNo", source = "plantDetails.netMeterSerialNo")
    CustomerResponseDto toResponse(Customer customer);
}
