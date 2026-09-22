package com.example.java_template.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validate độ dài tối đa, áp dụng cho cả {@link String} và {@link java.util.Collection}
 * (mỗi phần tử String trong collection đều phải thoả mãn).
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxLengthAdvancedValidator.class)
@Documented
public @interface MaxLengthAdvanced {

    int value();

    String fieldName() default "";

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
