package com.example.java_template.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Khuôn dữ liệu phân trang. Dùng để bọc kết quả trả về cho các API list/search.
 *
 * <p>{@link #of(Page)} ánh xạ từ {@link Page} của Spring Data sang DTO của project để
 * tránh lộ field nội bộ (vd {@code pageable}, {@code sort}, {@code numberOfElements}, ...).</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {

    List<T> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean last;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
