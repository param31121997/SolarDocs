package com.solardocs.api.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequestDto(@NotBlank String status) {}
