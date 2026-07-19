package com.solardocs.domain.license;

public record License(String key, String machineFingerprint, String activatedAt, String status) {}
