package com.dazzle.asklepios.service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DiagnosticTestValidator.class)
@Documented
public @interface ValidDiagnosticTest {
    String message() default "Invalid diagnostic test fields";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}