package com.solardocs.api.customer.dto;

import com.solardocs.api.common.validation.Pincode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCustomerRequestDto(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\d{10}") String mobile,
        String addressLine,
        String village,
        String district,
        String state,
        @Pincode String pincode
) {}
