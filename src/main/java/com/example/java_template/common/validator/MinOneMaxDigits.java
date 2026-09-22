package com.example.java_template.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validate Number trong khoảng {@code [1, 10^max - 1]} — tức là phải >= 1
 * và có tối đa {@code max} chữ số.
 *
 * <p>Ví dụ: {@code @MinOneMaxDigits(max = 6, fieldName = "Số tiền")} -> nhận từ 1 đến 999999.</p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinOneMaxDigitsValidator.class)
@Documented
public @interface MinOneMaxDigits {

    int max();

    String fieldName();

    /** Bỏ qua — validator tự generate. */
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
