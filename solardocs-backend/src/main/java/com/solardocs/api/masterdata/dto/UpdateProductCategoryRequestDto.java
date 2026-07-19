package com.solardocs.api.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductCategoryRequestDto(
        @NotBlank @Size(max = 100) String categoryName,
        @Size(max = 500) String description
) {}
