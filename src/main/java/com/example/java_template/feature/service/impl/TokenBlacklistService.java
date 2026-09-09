package com.example.java_template.feature.service.impl;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Thread-safe set lưu các token đã bị revoke
    private final Set<String> blackListedTokens = ConcurrentHashMap.newKeySet();

    //Thêm token vào blacklist (khi user logout)
    public void addBlacklistedToken(String token) {
        blackListedTokens.add(token);
    }

    // kiểm tra token có trong blackllist không
    public boolean isTokenBlacklisted(String token) {
        return blackListedTokens.contains(token);
    }

    // xóa token khỏi danh sách blackList dùng trong trường hợp muốn xóa hẳn trong blackedList
    public void  removeBlacklistedToken(String token) {
        blackListedTokens.remove(token);
    }

    // Số lượng token đang bị bllackList
    public int countBlacklistedTokens() {
        return blackListedTokens.size();
    }
}
