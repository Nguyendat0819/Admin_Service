package com.example.java_template.feature.model.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Kết quả trả về từ tầng service khi login/refresh.
 *
 * <p>Bao gồm cả 2 token để controller quyết định xử lý:
 * <ul>
 *   <li>{@code accessToken} → trả trong body cho client</li>
 *   <li>{@code refreshToken} → set vào HttpOnly cookie</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenPair {
    /** Access token ngắn hạn — client lưu memory, gửi qua Authorization header. */
    String accessToken;

    /** Refresh token dài hạn — controller set vào HttpOnly cookie. */
    String refreshToken;

    /** Loại token (luôn là "Bearer"). */
    @Builder.Default
    String type = "Bearer";

    /** Thời gian sống access token (giây). */
    Long expiresIn;

    /** Convert thành LoginResponse (chỉ chứa accessToken trong body). */
    public LoginResponse toLoginResponse() {
        return LoginResponse.builder()
                .token(accessToken)
                .type(type)
                .expiresIn(expiresIn)
                .build();
    }
}
