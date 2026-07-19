package com.solardocs.domain.common;

public record Address(
        String addressLine,
        String village,
        String district,
        String state,
        String pincode
) {}
