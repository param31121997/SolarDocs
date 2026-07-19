package com.solardocs.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GstinValidator implements ConstraintValidator<Gstin, String> {
    private static final String GSTIN_REGEX = "^\\d{2}[A-Z]{5}\\d{4}[A-Z]{1}[A-Z\\d]{1}Z[A-Z\\d]{1}$";
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || value.matches(GSTIN_REGEX);
    }
}
