package com.example.java_template.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {
    // Chuyển đổi chuỗi khóa bí mật dạng Base64 từ file cấu hình thành một đối tượng SecretKey chuẩn dùng cho thuật toán mã hóa đối xứng.
    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret-base64}") String secretBase64) {
        //giải mã chuỗi này thành mảng byte (byte[]).
        byte[] secret = Base64.getDecoder().decode(secretBase64);
        if (secret.length < 32) {
            throw new IllegalArgumentException("JWT Ký bí mật không được dưới 32 Byte");
        }
        // Trả về đối tượng SecretKeySpec được định cấu hình với thuật toán "HmacSHA256"
        return new SecretKeySpec(secret, "HmacSHA256");
    }
    // Nhiệm vụ: Tạo ra một bean JwtEncoder để phát hành (ký) token mới.
    @Bean
    // Inject bean SecretKey đã được tạo ở hàm trên vào đây.
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        // Sử dụng NimbusJwtEncoder với khóa bí mật đó để cấu hình bộ mã hóa.
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                // Thiết lập thuật toán ký là HS256 (MacAlgorithm.HS256).
                .algorithm(MacAlgorithm.HS256)
                .build();
    }
    //Nhiệm vụ: Tạo ra một bean JwtDecoder để giải mã và xác thực token mỗi khi client gửi kèm trong các request tiếp theo.
    //Nhận vào SecretKey (để kiểm tra chữ ký) và issuer (đơn vị phát hành được lấy từ file cấu hình qua ${app.jwt.issuer}).
    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            @Value("${app.jwt.issuer}") String issuer) {
//        Khởi tạo NimbusJwtDecoder với thuật toán HS256
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
//        Thiết lập bộ kiểm tra -> đảm bảo iss bên trong payload khớp cấu hình hệ thống , kiểm tra hsd exp
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
