package com.example.java_template.common.exception;

import com.example.java_template.common.response.DomainCode;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(Object... args) {
        super(DomainCode.NOT_FOUND, args);
    }
}
