package com.solardocs.api.masterdata.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ItemResponseDto(
        String id,
        String itemName,
        String description,
        String type,
        String unit,
        BigDecimal defaultRate,
        String defaultGstPercent,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
