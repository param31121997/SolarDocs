package com.solardocs.api.masterdata.dto;

import java.time.Instant;

public record ProductCategoryResponseDto(
        String id,
        String categoryCode,
        String categoryName,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
