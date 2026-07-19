package com.solardocs.domain.template;

public record DocumentTemplate(
        String code,
        String name,
        String version,
        String category,
        String htmlFile,
        boolean active
) {}
