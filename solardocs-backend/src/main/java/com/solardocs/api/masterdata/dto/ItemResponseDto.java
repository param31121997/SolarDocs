package com.solardocs.api.masterdata.dto;

import java.time.Instant;

public record ItemResponseDto(
        String id,
        String itemName,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
