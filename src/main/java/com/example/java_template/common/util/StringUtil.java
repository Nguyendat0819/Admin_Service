package com.example.java_template.common.util;

import lombok.experimental.UtilityClass;

import java.text.Normalizer;
import java.time.LocalDateTime;

import static com.example.java_template.common.util.Const.FILE_TIME_FORMAT;

/**
 * Tiện ích xử lý chuỗi: bỏ dấu, chuẩn hoá tên file xuất ra theo dấu thời gian.
 */
@UtilityClass
public final class StringUtil {

    /**
     * Bỏ dấu Tiếng Việt và gộp nhiều khoảng trắng liên tiếp thành 1.
     */
    public static String normalizeVi(String s) {
        if (s == null) return null;
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);

        String noDiacritics = nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        noDiacritics = noDiacritics
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return noDiacritics.replaceAll("\\s+", " ").trim();
    }

    /**
     * So sánh không phân biệt hoa thường + bỏ dấu Tiếng Việt.
     */
    public static boolean equalsViIgnoreCaseNoAccent(String a, String b) {
        if (a == null || b == null) return true;
        String na = normalizeVi(a).toLowerCase();
        String nb = normalizeVi(b).toLowerCase();
        return na.equals(nb);
    }

    /**
     * Sinh tên file xuất ra: bỏ dấu + thay ký tự đặc biệt + ghép timestamp.
     * Tránh trùng tên khi client xuất nhiều file cùng lúc.
     */
    public static String removeVietnameseAccentWithTime(String input) {
        if (input == null || input.isBlank()) {
            return "file_" + LocalDateTime.now().format(FILE_TIME_FORMAT);
        }

        int dotIndex = input.lastIndexOf('.');
        String name = dotIndex > 0 ? input.substring(0, dotIndex) : input;
        String ext = dotIndex > 0 ? input.substring(dotIndex) : "";

        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace("đ", "d")
                .replace("Đ", "D");

        normalized = normalized
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_+", "_");

        String timestamp = LocalDateTime.now().format(FILE_TIME_FORMAT);
        return normalized + "_" + timestamp + ext;
    }
}
