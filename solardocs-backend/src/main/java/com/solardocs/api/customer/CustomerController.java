package com.solardocs.api.customer;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.api.customer.dto.*;
import com.solardocs.api.customer.mapper.CustomerIndexMapper;
import com.solardocs.api.customer.mapper.CustomerMapper;
import com.solardocs.application.customer.CustomerSearchService;
import com.solardocs.application.customer.CustomerService;
import com.solardocs.domain.common.Address;
import com.solardocs.domain.customer.Customer;
import com.solardocs.domain.customer.CustomerStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerSearchService searchService;
    private final CustomerMapper customerMapper;
    private final CustomerIndexMapper customerIndexMapper;

    public CustomerController(CustomerService customerService, CustomerSearchService searchService,
                              CustomerMapper customerMapper, CustomerIndexMapper customerIndexMapper) {
        this.customerService = customerService;
        this.searchService = searchService;
        this.customerMapper = customerMapper;
        this.customerIndexMapper = customerIndexMapper;
    }

    @PostMapping
    public ApiResponse<CustomerResponseDto> create(@Valid @RequestBody CreateCustomerRequestDto req) {
        Customer c = customerService.create(req.name(), req.mobile(),
                new Address(req.addressLine(), req.village(), req.district(), req.state(), req.pincode()));
        return ApiResponse.ok(customerMapper.toResponse(c));
    }

    @GetMapping
    public ApiResponse<List<CustomerSummaryResponseDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String village,
            @RequestParam(required = false) String district) {
        return ApiResponse.ok(searchService.search(q, status, village, district).stream()
                .map(customerIndexMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponseDto> get(@PathVariable String id) {
        return ApiResponse.ok(customerMapper.toResponse(customerService.get(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponseDto> update(@PathVariable String id, @Valid @RequestBody UpdateCustomerRequestDto req) {
        Customer c = customerService.update(id, req.name(), req.mobile(), req.alternateMobile(),
                new Address(req.addressLine(), req.village(), req.district(), req.state(), req.pincode()),
                req.consumerNumber(), req.applicationNumber(), req.sanctionedLoadKw(), req.plantCapacityKw(),
                req.discom(), req.category());
        return ApiResponse.ok(customerMapper.toResponse(c));
    }

    @PutMapping("/{id}/plant-details")
    public ApiResponse<CustomerResponseDto> updatePlantDetails(@PathVariable String id,
                                                                 @RequestBody UpdatePlantDetailsRequestDto req) {
        var details = new com.solardocs.domain.customer.PlantInstallationDetails(
                req.email(), req.installationDate(), req.inverterMake(), req.inverterRating(),
                req.inverterCapacityKw(), req.chargeControllerType(), req.hpd(),
                req.earthing1Ohms(), req.earthing2Ohms(), req.earthing3Ohms(),
                req.moduleWattage(), req.moduleCount(), req.moduleCapacityKw(), req.moduleSerialNumbers(),
                req.cellManufacturerName(), req.cellGstInvoiceNo(), req.aadhaarNumber(),
                req.inspectionDate(), req.inspectionLetterNo(), req.inspectionLetterDate(),
                req.agreementPlace(), req.netMeterSerialNo()
        );
        Customer c = customerService.updatePlantDetails(id, details);
        return ApiResponse.ok(customerMapper.toResponse(c));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CustomerResponseDto> updateStatus(@PathVariable String id, @Valid @RequestBody UpdateStatusRequestDto req) {
        Customer c = customerService.updateStatus(id, CustomerStatus.valueOf(req.status()));
        return ApiResponse.ok(customerMapper.toResponse(c));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> archive(@PathVariable String id) {
        customerService.archive(id);
        return ApiResponse.ok(null);
    }
}
