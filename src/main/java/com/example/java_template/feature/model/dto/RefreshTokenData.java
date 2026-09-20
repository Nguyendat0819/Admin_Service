package com.example.java_template.feature.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Thông tin refresh token lưu trong Redis.
 *
 * <p>Được lưu dưới dạng JSON tại key {@code refresh_token:<jwt>}.</p>
 *
 * <p><b>Family</b>: UUID gắn liền với 1 phiên đăng nhập (từ lúc login).
 * Mỗi lần refresh, tạo token mới cùng family, token cũ bị đánh dấu {@code used=true}.</p>
 *
 * <p><b>Used flag</b>: khi token đã được rotate, dùng lại nó → phát hiện đánh cắp
 * → vô hiệu hóa toàn bộ family.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenData {
    /** Username trong token. */
    String username;

    /** Role trong token. */
    String role;

    /** Family ID - nhóm các token cùng phiên đăng nhập. */
    String family;

    /** Token value của lần rotate trước (phục vụ debug / audit). */
    String previous;

    /** True sau khi đã rotate - dùng lại sẽ bị coi là đánh cắp. */
    boolean used;

    /** Epoch millis lúc tạo. */
    long createdAt;
}
