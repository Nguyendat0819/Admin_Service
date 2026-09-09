package com.example.java_template.common.exception;

import com.example.java_template.common.response.DomainCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.slf4j.helpers.MessageFormatter;
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BaseException extends RuntimeException {
    DomainCode domainCode; // Mã lỗi và Template tin nhắn
    transient Object[] args; // Dữ liệu động truyền vào
    protected BaseException(DomainCode domainCode, Object... args) {
        super(MessageFormatter.arrayFormat(domainCode.getMessage(), args).getMessage()); // Không tìm thấy người dùng có tên là: Nguyễn Văn A"
        this.domainCode = domainCode; // truyền vào domainCode
        this.args = args; // truyền vào dữ liệu động
    }
}
