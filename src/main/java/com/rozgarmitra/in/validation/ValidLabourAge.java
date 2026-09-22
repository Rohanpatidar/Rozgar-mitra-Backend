package com.rozgarmitra.in.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LabourAgeValidator.class)
public @interface ValidLabourAge {
    String message() default "Labours must be at least 18 years old";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
