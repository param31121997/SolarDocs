package com.solardocs.api.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateItemRequestDto(
        @NotBlank @Size(max = 150) String itemName,
        @Size(max = 500) String description
) {}
