package com.solardocs.api.customer.dto;

public record CustomerSummaryResponseDto(
        String customerId,
        String name,
        String mobile,
        String status,
        String village,
        String consumerNumber,
        String applicationNumber
) {}
