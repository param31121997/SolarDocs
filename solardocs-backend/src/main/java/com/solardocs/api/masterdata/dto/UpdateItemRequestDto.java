package com.solardocs.api.masterdata.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateItemRequestDto(
        @NotBlank @Size(max = 150) String itemName,
        @Size(max = 500) String description,
        @Size(max = 100) String type,
        @Size(max = 30) String unit,
        @DecimalMin(value = "0", inclusive = true) BigDecimal defaultRate,
        @Size(max = 10) String defaultGstPercent
) {}
