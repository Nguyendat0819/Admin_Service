package com.example.java_template.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

public class AtLeastOneValidator implements ConstraintValidator<AtLeastOne, Collection<?>> {

    @Override
    public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
        boolean valid = value != null && !value.isEmpty();

        if (!valid) {
            context.disableDefaultConstraintViolation();

            String fieldName = context.getDefaultConstraintMessageTemplate();
            if (fieldName == null || fieldName.isEmpty()) {
                fieldName = "Trường này";
            }

            context.buildConstraintViolationWithTemplate(
                    "Phải có ít nhất 1 " + fieldName
            ).addConstraintViolation();
        }
        return valid;
    }
}
