package com.solardocs.api.customer.dto;

import com.solardocs.api.common.validation.Pincode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record UpdateCustomerRequestDto(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\d{10}") String mobile,
        @Pattern(regexp = "^$|\\d{10}") String alternateMobile,
        String email,
        String aadhaarNumber,
        String addressLine,
        String village,
        String district,
        String state,
        @Pincode String pincode,
        String consumerNumber,
        String applicationNumber,
        BigDecimal sanctionedLoadKw,
        BigDecimal plantCapacityKw,
        String discom,
        String category
) {}
