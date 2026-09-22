package com.example.java_template.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class MinOneMaxDigitsValidator implements ConstraintValidator<MinOneMaxDigits, Number> {

    private long maxValue;
    private String fieldName;

    @Override
    public void initialize(MinOneMaxDigits annotation) {
        this.fieldName = annotation.fieldName();
        this.maxValue = (long) Math.pow(10, annotation.max()) - 1;
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) return true;

        long val = (value instanceof BigDecimal bd) ? bd.longValue() : value.longValue();
        boolean valid = val >= 1 && val <= maxValue;

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    fieldName + " phải từ 1 và tối đa " + String.valueOf(maxValue).length() + " chữ số"
            ).addConstraintViolation();
        }
        return valid;
    }
}
