package com.solardocs.domain.document;

import java.time.Instant;

public record UploadedDocument(String id, String type, String fileName, String filePath, Instant uploadedAt) {}
