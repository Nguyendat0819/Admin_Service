package com.example.java_template.common.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DTO tiêu chuẩn cho các request list/search có phân trang.
 *
 * <p>Field {@code search} — chuỗi filter chung; client tuỳ ý dùng.
 * {@code sort}/{@code sorts}+{@code direction} — hỗ trợ cả single và multi sort.</p>
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageDto {

    /** Chỉ số trang (0-based). {@code null} -> dùng {@code Const.PAGE_DEFAULT}. */
    private Integer page;

    /** Kích thước trang. {@code null} -> dùng {@code Const.SIZE_DEFAULT}; bị kẹp trần {@code Const.SIZE_MAX}. */
    private Integer size;

    /** Từ khoá filter chung. */
    private String search;

    /** Cột sort (single). */
    private String sort;

    /** Danh sách cột sort (multi). Mỗi phần tử dạng {@code "col,direction"}. */
    private List<String> sorts;

    /** Hướng sort (single): {@code ASC} | {@code DESC}. */
    private String direction;
}
