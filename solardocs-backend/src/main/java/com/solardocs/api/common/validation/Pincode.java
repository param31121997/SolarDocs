package com.solardocs.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PincodeValidator.class)
public @interface Pincode {
    String message() default "Invalid 6-digit PIN code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
