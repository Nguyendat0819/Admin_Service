package com.example.java_template.common.response;

import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
@Component // Spring tự động tạo và quản lý đối tượng
public class ApiResponseFactory {

    public <T> ApiResponse<T> of(DomainCode code, T data, Object ... args) {
        return ApiResponse.<T>builder()
                .transactionTime(LocalDateTime.now()) // : Tự động lấy thời gian hiện tại
                .code(code.getCode()) // Tự động lấy từ DomainCode
                .message(formatMessage(code.getMessage(),args)) //  format nội dung {} bằng args nếu có
                .traceId(currentTraceId()) //Tự động trích xuất từ Log Context (MDC) thông qua hàm currentTraceId()
                .data(data) // Dữ liệu trả về (có thể là một Object, List, hoặc null).
                .build();

    }

    public ApiResponse<Void> success(){ // trả thành công không kèm theo dữ liệu
        return of(DomainCode.SUCCESS,null,(Object) null);
    }
    public <T> ApiResponse<T> success(T data){ // trả thành công kèm theo dữ liệu
        return of(DomainCode.SUCCESS,data,(Object) null);
    }

    public ApiResponse<Void> error(DomainCode code){ // trả về lỗi tĩnh kèm theo message cố định
        return of(code,null,(Object) null);
    }

    public ApiResponse<Void> error(DomainCode code,Object ... args){ // trả về lỗi kèm theo domain và message động
        return of(code,null, args);
    }

    private String formatMessage(String template, Object... args) {
        if (args == null || args.length == 0) {
            return template.replace("{}","").trim();
        }
        return MessageFormatter.arrayFormat(template, args).getMessage();
    }

    private String currentTraceId() {
        return MDC.get("traceId");
    }
}
