package com.solardocs.api.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CustomerResponseDto(
        String customerId, String name, String mobile, String alternateMobile,
        String addressLine, String village, String district, String state, String pincode,
        String consumerNumber, String applicationNumber,
        BigDecimal sanctionedLoadKw, BigDecimal plantCapacityKw,
        String discom, String category, String status,
        Instant createdAt, Instant updatedAt
) {
}
