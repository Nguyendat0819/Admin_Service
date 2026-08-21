package com.example.java_template.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public enum DomainCode {

    //  Success
    SUCCESS("000", HttpStatus.OK, "Thành công"),

    //  Lỗi client (4xx)
    BAD_REQUEST("400", HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ: {}"),
    VALIDATION_ERROR("401", HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ: {}"),
    INVALID_PARAMETER("402", HttpStatus.BAD_REQUEST, "Tham số không hợp lệ: {}"),
    UNAUTHORIZED("403", HttpStatus.UNAUTHORIZED, "Chưa xác thực"),
    TOKEN_EXPIRED("404", HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn"),
    FORBIDDEN("405", HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    NOT_FOUND("406", HttpStatus.NOT_FOUND, "Không tìm thấy: {}"),
    CONFLICT("407", HttpStatus.CONFLICT, "Dữ liệu đã tồn tại hoặc đang xung đột: {}"),
    IMPORT_VALIDATION_ERROR("408", HttpStatus.BAD_REQUEST, "File import có lỗi: {}"),
    IMPORT_FILE_INVALID("409", HttpStatus.BAD_REQUEST, "File import không đúng định dạng: {}"),

    //  Lỗi server / service ngoài (5xx)
    INTERNAL_ERROR("500", HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau"),
    EXTERNAL_SERVICE_ERROR("502", HttpStatus.BAD_GATEWAY, "Lỗi khi gọi dịch vụ ngoài: {}"),
    SERVICE_UNAVAILABLE("503", HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ tạm thời không khả dụng");

    //  Mã nghiệp vụ riêng (thêm theo service)
    // Ví dụ: SAMPLE_NOT_FOUND("1001", HttpStatus.NOT_FOUND, "Không tìm thấy sample id {}"),


    String code;
    HttpStatus httpStatus;
    String message;
}
