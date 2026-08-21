package com.example.java_template.common.exception;

import com.example.java_template.common.response.DomainCode;
import org.slf4j.helpers.MessageFormatter;

public class BaseException extends RuntimeException {
    DomainCode domainCode;
    transient Object[] args;
    protected BaseException(DomainCode domainCode, Object... args) {
        super(MessageFormatter.arrayFormat(domainCode.getMessage(), args).getMessage());
        this.domainCode = domainCode;
        this.args = args;
    }
}
