package com.example.java_template.common.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

// Cấu hình CORS: những origin FE được gọi API từ trình duyệt. Thêm hay bớt origin là sửa yaml rồi restart.
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    // Origin đầy đủ scheme + host + cổng (vd http://localhost:4200), khớp nguyên văn, không nhận dấu *.
    // Biến môi trường APP_CORS_ALLOWED_ORIGINS ghi đè bằng chuỗi các origin cách nhau dấu phẩy.
    @NotEmpty
    private List<@NotBlank String> allowedOrigins;
}