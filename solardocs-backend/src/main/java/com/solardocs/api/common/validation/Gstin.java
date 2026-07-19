package com.solardocs.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GstinValidator.class)
public @interface Gstin {
    String message() default "Invalid GSTIN format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
