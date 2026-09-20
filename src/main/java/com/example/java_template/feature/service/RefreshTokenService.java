package com.example.java_template.feature.service;

import com.example.java_template.feature.model.dto.RefreshTokenData;
import com.example.java_template.feature.service.impl.RefreshTokenServiceImpl;

/**
 * Hợp đồng quản lý refresh token: create / rotate / revoke + theft detection.
 *
 * <p>Triển khai mặc định dùng Redis: {@link RefreshTokenServiceImpl}.</p>
 *
 * <p><b>Luật chơi:</b></p>
 * <ul>
 *   <li>Mỗi refresh token mang theo {@code familyId} - nhóm các token cùng phiên login.</li>
 *   <li>Khi rotate: token cũ bị đánh dấu {@code used=true}, token mới cùng family được sinh ra.</li>
 *   <li>Nếu token đã {@code used} bị tái sử dụng → phát hiện đánh cắp → vô hiệu hóa toàn bộ family.</li>
 * </ul>
 */
public interface RefreshTokenService {

    /**
     * Kết quả trả về từ {@link #create(String, String)} và {@link #rotate(String)}.
     */
    record IssuedToken(String token, RefreshTokenData data) {}

    /**
     * Tạo refresh token mới + family mới (gọi khi login).
     */
    IssuedToken create(String username, String role);

    /**
     * Rotate refresh token: tạo token mới, đánh dấu token cũ đã dùng.
     *
     * @throws com.example.java_template.common.exception.BusinessException
     *         nếu token không tồn tại (expired/revoked) hoặc đã bị đánh cắp.
     */
    IssuedToken rotate(String oldToken);

    /**
     * Vô hiệu hóa 1 refresh token (gọi khi logout).
     */
    void revoke(String token);

    /**
     * Vô hiệu hóa TOÀN BỘ family (theft detection, admin force logout...).
     */
    void revokeFamily(String familyId);
}
