package com.solardocs.domain.document;

import java.time.Instant;

public record GeneratedDocument(String id, String templateCode, String templateVersion, String filePath, Instant generatedAt) {}
