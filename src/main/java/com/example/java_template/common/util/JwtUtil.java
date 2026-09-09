package com.example.java_template.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.java_template.common.exception.BusinessException;
import com.example.java_template.common.response.DomainCode;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Lớp tiện ích hỗ trợ các thao tác liên quan đến JSON Web Token (JWT).
 * Cung cấp các chức năng: tạo token, trích xuất thông tin từ token và xác thực
 * token.
 */
@Component
public class JwtUtil {
    /**
     * Khóa bí mật dùng để ký và xác thực JWT.
     * Được lấy từ cấu hình application.yml (mặc định nếu không có sẽ dùng chuỗi dự
     * phòng).
     * Yêu cầu độ dài khóa phải đủ lớn (ví dụ >= 256 bits cho thuật toán HS256).
     */
    @Value("${jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
    private String jwtSecret;

    /**
     * Thời gian sống của token (tính bằng mili-giây).
     * Được cấu hình trong application.yml.
     */
    @Value("${jwt.expiration:3600}")
    private Long expiration;

    /**
     * Chuyển đổi chuỗi bí mật (jwtSecret) thành đối tượng SecretKey.
     * Sử dụng thuật toán HMAC-SHA để tạo khóa mã hóa chuẩn dùng cho thư viện jjwt.
     * 
     * @return Đối tượng SecretKey dùng để ký và giải mã token.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo ra một JWT token mới cho người dùng sau khi đăng nhập thành công.
     * 
     * @param username Tên đăng nhập của người dùng sẽ được lưu vào payload của
     *                 token.
     * @return Chuỗi JWT đã được ký.
     */
    public String generateToken(String username) {
        Date now = new Date(); // Khởi tạo thời gian hiện tại
        Date expiryDate = new Date(now.getTime() + expiration); // hạn hết thời gian
        return Jwts.builder()
                .subject(username) // cho username
                .issuedAt(now) // thoi điểm bắt đầu
                .expiration(expiryDate) // hạn thời gian
                .signWith(getSigningKey()) // tạo ra jwt
                .compact(); //
    }

    /**
     * Giải mã token và trích xuất thông tin tên đăng nhập (Subject) từ payload.
     * 
     * @param token Chuỗi JWT cần giải mã.
     * @return Tên đăng nhập (username) được lưu trong token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()) // xacs nhận từ khóa đăng ký
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Xác thực xem JWT token có hợp lệ hay không.
     * Kiểm tra đối chiếu chữ ký, thời hạn sử dụng và định dạng của token.
     * 
     * @param token Chuỗi JWT cần kiểm tra.
     * @return true nếu token hoàn toàn hợp lệ.
     * @throws BusinessException Ném ra lỗi nghiệp vụ với mã code tương ứng nếu
     *                           token bị lỗi (hết hạn, sai chữ ký, v.v.)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(DomainCode.TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
    }

    // Lấy thời gian hết hạn
    public long getExpiration() {
        return expiration;
    }

    /**
     * Trích xuất token từ HttpServletRequest.
     * Token được gửi trong header "Authorization" với format: "Bearer <token>"
     *
     * @param request HttpServletRequest chứa header Authorization
     * @return Chuỗi JWT token (đã loại bỏ prefix "Bearer ")
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
