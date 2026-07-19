package com.solardocs.domain.security;

public record PinCredential(String pinHash, String salt) {}
