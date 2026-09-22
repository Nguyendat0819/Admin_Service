package com.example.java_template.common.exception;


import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.common.response.ApiResponseFactory;
import com.example.java_template.common.response.DomainCode;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor

public class GlobalExceptionHandler  {
  private final ApiResponseFactory apiResponseFactory;
  private final Environment environment;

    // lỗi nghiệp vụ (BusinessException, ResourceNotFoundException...)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBusiness(BaseException e){
        log.warn("handleBusiness:{}",e.getMessage());
        ApiResponse<?> body = apiResponseFactory.error(e.getDomainCode(), e.getMessage());
        return ResponseEntity
                .status(e.getDomainCode().getHttpStatus())
                .body(body);
    }

//    Lỗi 403- không có quyền
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccesDenid(AccessDeniedException e){
        log.warn("handleAccessDenied:{}",e.getMessage());
        ApiResponse<?> body = apiResponseFactory.error(DomainCode.FORBIDDEN);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }

//    401- chưa xác thực
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthentication(AuthenticationException e){
        log.warn("handleAuthentication:{}",e.getMessage());
        ApiResponse<?> body = apiResponseFactory.error(DomainCode.UNAUTHORIZED);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

//    400- validate @Valid body. data = {field: lý do}, {} trong message = danh sách field sai
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e){
        Map<String, String> fieldErrors = new LinkedHashMap<>(); // giữ nguyên các thứ tự lỗi được đưa vào
        e.getBindingResult().getFieldErrors()
                        .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        log.warn("handleValidation:{}",e.getMessage());
        ApiResponse<?> body = apiResponseFactory.of(DomainCode.VALIDATION_ERROR,fieldErrors,String.join(",",fieldErrors.keySet()));
        return ResponseEntity
                .status(DomainCode.VALIDATION_ERROR.getHttpStatus())
                .body(body);
    }


    // 400 - validate @RequestParam/@PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(ConstraintViolationException e){
        Map<String,String> fieldErrors = new LinkedHashMap<>();
        e.getConstraintViolations().forEach( v-> fieldErrors.put(v.getPropertyPath().toString(), v.getMessage()));
        log.warn("handleValidation:{}",e.getMessage());
        ApiResponse<?> body = apiResponseFactory.of(DomainCode.VALIDATION_ERROR,fieldErrors,String.join(",",fieldErrors.keySet()));
        return ResponseEntity
                .status(DomainCode.VALIDATION_ERROR.getHttpStatus())
                .body(body);
    }


    // 500 - fallback cho lỗi KHÔNG lường (bug/NPE...) -> log.error kèm stack trace, KHÔNG lộ chi tiết cho client
    // Ngoài môi trường prod thì kèm luôn nguyên nhân gốc vào response.
    //
    // Chỉ trả câu chung chung thì lỗi loại này (SpEL sai hằng, DML thiếu @Modifying...) không để
    // lại dấu vết nào ở phía gọi, phải mò trong log ứng dụng mới biết hỏng ở đâu.

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAll(Exception e){
        log.error("handleAll:{}",e.getMessage());
        ApiResponse<?> body = environment.matchesProfiles("prod") // khi dùng tron môi trường product
                ? apiResponseFactory.error(DomainCode.INTERNAL_ERROR) // trả về lỗi hệ thống tránh rò gỉ dữ liệu
                : apiResponseFactory.of(DomainCode.INTERNAL_ERROR,rootCause(e),null);
        return ResponseEntity
                .status(DomainCode.INTERNAL_ERROR.getHttpStatus())
                .body(body);
    }

//    Trong Java, các exception thường được "bọc" (wrap) nhiều lớp bên trong nhau. Ví dụ: Một lỗi sai cú pháp SQL ở tầng Database sẽ bị bọc lại bởi Hibernate, rồi tiếp tục bị Spring bọc lại thành DataAccessException, và quăng ra Controller.
    private String rootCause(Throwable e) {
        Throwable cause = e;

        // Vòng lặp này cứ tiếp tục lấy lỗi bên trong (cause.getCause())
        // cho đến khi chạm đến lớp lõi cuối cùng (getCause() == null).
        // Điều kiện (cause.getCause() != cause) dùng để phòng ngừa trường hợp exception tự trỏ lại chính nó gây lặp vô tận (rất hiếm nhưng có thể xảy ra).
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        // Trả về chuỗi kết hợp giữa Tên loại lỗi và Câu thông báo lỗi.
        // VD: "NullPointerException: Cannot invoke method on null object" hoặc "SQLSyntaxErrorException: Table 'users' doesn't exist"
        return cause.getClass().getSimpleName() + ": " + cause.getMessage();
    }
}
