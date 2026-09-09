package com.example.java_template.feature.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @NotBlank(message = "tài khoản không được để trống")
    String username;
    @NotBlank(message = "mật khẩu không được để trống")
    String password;
}
