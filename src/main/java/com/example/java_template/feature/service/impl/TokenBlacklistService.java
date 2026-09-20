package com.example.java_template.feature.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    /**
     * Lưu token kèm thời điểm hết hạn (epoch millis).
     * Khi current time > expiration → token hết hạn, có thể xóa khỏi map.
     * Lý do dùng Map thay vì Set: cần biết expiration để cleanup.
     */
    private final Map<String, Long> blackListedTokens = new ConcurrentHashMap<>();

    /**
     * Thêm token vào blacklist (khi user logout hoặc refresh).
     * @param token JWT token cần blacklist
     * @param expirationEpochMillis thời điểm hết hạn của token (lấy từ claim `exp`)
     */
    public void addBlacklistedToken(String token, long expirationEpochMillis) {
        blackListedTokens.put(token, expirationEpochMillis);
    }

    /**
     * Backward-compatible overload — dùng khi không có expiration (mặc định +24h).
     */
    public void addBlacklistedToken(String token) {
        long defaultExp = Instant.now().plusSeconds(86400).toEpochMilli();
        blackListedTokens.put(token, defaultExp);
    }

    /**
     * Kiểm tra token có đang bị blacklist KHÔNG (chưa hết hạn blacklist) không.
     *
     * <p>Hành vi:</p>
     * <ul>
     *   <li>Token không có trong map → false (chưa từng bị blacklist)</li>
     *   <li>Token có trong map nhưng entry đã hết hạn → false
     *       (đồng thời dọn entry rác — lazy cleanup)</li>
     *   <li>Token có trong map và entry còn hạn → true (đang bị chặn)</li>
     * </ul>
     *
     * <p>⚠️ Lưu ý bảo mật: Nếu hàm này trả về <b>false</b> vì entry hết hạn,
     * caller KHÔNG được mặc định coi là "an toàn". Caller cần tự xử lý:
     * ví dụ với refresh → phải add lại token cũ vào blacklist (rotation).</p>
     */
    public boolean isTokenBlacklisted(String token) {
        Long exp = blackListedTokens.get(token);
        if (exp == null) {
            return false;
        }
        // Lazy cleanup: nếu blacklist entry đã quá hạn → xóa và trả false
        // Dùng > (không >=) để đồng bộ với jjwt: jjwt coi exp là "còn hạn" khi now <= exp
        if (Instant.now().toEpochMilli() > exp) {
            // remove(key, value) chỉ xóa nếu value vẫn khớp — tránh race với thread khác
            blackListedTokens.remove(token, exp);
            return false;
        }
        return true;
    }

    /**
     * Xóa token khỏi blacklist (dùng khi muốn gỡ thủ công, ví dụ admin gỡ).
     */
    public void removeBlacklistedToken(String token) {
        blackListedTokens.remove(token);
    }

    /**
     * Số lượng token đang bị blacklist (bao gồm cả token đã hết hạn chưa cleanup).
     */
    public int countBlacklistedTokens() {
        return blackListedTokens.size();
    }

    /**
     * Scheduled cleanup: chạy mỗi 5 phút, xóa toàn bộ token đã hết hạn.
     * Tránh để blacklist phình to vô hạn.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 phút
    public void cleanupExpiredTokens() {
        long now = Instant.now().toEpochMilli();
        int before = blackListedTokens.size();
        blackListedTokens.entrySet().removeIf(entry -> entry.getValue() <= now);
        int removed = before - blackListedTokens.size();
        if (removed > 0) {
            System.out.println("[TokenBlacklistService] Cleaned up " + removed
                    + " expired tokens. Remaining: " + blackListedTokens.size());
        }
    }
}
