package com.solardocs.domain.vendor;

public record VendorProfile(
        String companyName,
        String gstin,
        String registeredAddress,
        String logoPath,
        String bankAccountName,
        String bankAccountNumber,
        String bankIfsc,
        String signatoryName
) {}
