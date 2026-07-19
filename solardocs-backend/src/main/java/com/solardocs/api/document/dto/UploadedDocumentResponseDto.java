package com.solardocs.api.document.dto;

import java.time.Instant;

public record UploadedDocumentResponseDto(
        String id,
        String type,
        String fileName,
        Instant uploadedAt
) {}
