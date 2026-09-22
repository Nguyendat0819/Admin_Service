package com.example.java_template.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

public class MaxLengthAdvancedValidator implements ConstraintValidator<MaxLengthAdvanced, Object> {

    private int max;
    private String fieldName;

    @Override
    public void initialize(MaxLengthAdvanced annotation) {
        this.max = annotation.value();
        this.fieldName = annotation.fieldName();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        boolean valid = true;

        if (value instanceof String str) {
            valid = str.length() <= max;
        } else if (value instanceof Collection<?> col) {
            for (Object o : col) {
                if (o instanceof String s && s.length() > max) {
                    valid = false;
                    break;
                }
            }
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    (fieldName.isEmpty() ? "Trường này" : fieldName) + " vượt quá " + max + " ký tự"
            ).addConstraintViolation();
        }
        return valid;
    }
}
