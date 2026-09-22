package com.example.java_template.common.persistence;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Helper dùng chung cho truy vấn native query + phân trang.
 *
 * <p><b>Quy tắc an toàn:</b></p>
 * <ul>
 *     <li>Mọi giá trị truyền vào SQL phải qua {@link NamedParameterJdbcTemplate}
 *         (named param {@code :key}) hoặc whitelist ({@link #buildSafeOrder}).</li>
 *     <li>KHÔNG nối chuỗi giá trị từ user vào câu SQL (chống SQL injection).</li>
 * </ul>
 *
 * <p><b>Lưu ý:</b> Lớp abstract + KHÔNG gắn {@code @Repository}: đây là lớp cha cho các
 * {@code *RepositoryCustomImpl}, tự nó không phải một Spring bean. Nếu gắn
 * {@code @Repository} Spring sẽ dựng thêm 1 instance rỗng chẳng ai dùng.</p>
 */
@Slf4j
public abstract class NativeQuerySupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final AtomicLong KEY_SEQ = new AtomicLong();

    // ===== Query utilities =====

    /**
     * Sinh điều kiện {@code AND field = ANY(:key)} cho List + nạp giá trị vào params.
     * List rỗng -> trả chuỗi rỗng (AND được bỏ).
     */
    public static <T> String genSqlWhereIn(String field, List<T> values, Map<String, Object> params) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        String key = "param_" + field + "_" + KEY_SEQ.incrementAndGet();
        params.put(key, values);
        return " AND " + field + " = ANY(:" + key + ")";
    }

    public Map<String, Object> callProcedure(String procedureName, SqlParameterSource params) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withProcedureName(procedureName);
        return call.execute(params);
    }

    // ===== Pagination =====

    /**
     * {@link PageRequest} an toàn: client gửi {@code page < 0} hoặc {@code size < 1}
     * sẽ ném {@code IllegalArgumentException} (lỗi 500). Hàm này kẹp lại.
     * Trần {@code Const.SIZE_MAX} chặn một request kéo cả bảng.
     */
    public PageRequest getPage(com.example.java_template.common.persistence.dto.PageDto pageDto) {
        int page = pageDto.getPage() == null ? com.example.java_template.common.util.Const.PAGE_DEFAULT : pageDto.getPage();
        int size = pageDto.getSize() == null ? com.example.java_template.common.util.Const.SIZE_DEFAULT : pageDto.getSize();
        return PageRequest.of(
                Math.max(page, com.example.java_template.common.util.Const.PAGE_DEFAULT),
                Math.min(Math.max(size, 1), com.example.java_template.common.util.Const.SIZE_MAX));
    }

    // ===== LIKE helpers (Postgres) =====

    public String appendLikeExpressionById(String value) {
        return "%," + value.trim() + ",%";
    }

    public String appendLikeExpression(String value) {
        return "%" + value.trim() + "%";
    }

    /**
     * Postgres LIKE/ILIKE chỉ có 2 ký tự đại diện {@code %} và {@code _} (không có
     * {@code [ ]} như SQL Server). Ký tự thoát mặc định là {@code \\} nên phải thoát chính
     * nó TRƯỚC, không nó đi thoát nhầm ký tự sau.
     */
    public String appendLikeEscapee(String value) {
        if (StringUtils.isBlank(value)) {
            return "%%";
        }
        String escaped = value.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    // ===== SELECT =====

    public <T> T getFirstData(String sql, Map<String, Object> params, Class<T> clazz) {
        List<T> result = namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(clazz));
        if (result.isEmpty()) {
            throw new com.example.java_template.common.exception.ResourceNotFoundException(clazz.getSimpleName());
        }
        return result.get(0);
    }

    public <T> List<T> getList(String sql, Map<String, Object> params, Class<T> clazz) {
        log.info("[getList] params={}", params);
        return namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(clazz));
    }

    /** Trả tên field của class (kể cả field superclass) -> dùng làm whitelist cột cho ORDER BY. */
    public static Set<String> getAllowedColumns(Class<?> clazz) {
        Set<String> fields = new HashSet<>();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                fields.add(f.getName());
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Chỉ chấp nhận cột nằm trong whitelist + hướng {@code ASC}/{@code DESC} -> chống
     * SQL-injection ở ORDER BY. Multi sort ưu tiên trước single sort.
     */
    public static String buildSafeOrder(String sort, String direction, List<String> sorts, Class<?> clazz) {
        Set<String> allowed = getAllowedColumns(clazz);
        List<String> orderClauses = new ArrayList<>();

        if (sorts != null && !sorts.isEmpty()) {
            for (String s : sorts) {
                String[] parts = s.split(",");
                if (parts.length == 2) {
                    String col = parts[0].trim();
                    String dir = parts[1].trim();
                    if (allowed.contains(col)
                            && ("ASC".equalsIgnoreCase(dir) || "DESC".equalsIgnoreCase(dir))) {
                        orderClauses.add(camelToSnake(col) + " " + dir.toUpperCase());
                    }
                }
            }
        }

        if (orderClauses.isEmpty()
                && StringUtils.isNotBlank(sort)
                && allowed.contains(sort)
                && ("ASC".equalsIgnoreCase(direction) || "DESC".equalsIgnoreCase(direction))) {
            orderClauses.add(camelToSnake(sort) + " " + direction.toUpperCase());
        }

        return orderClauses.isEmpty() ? null : String.join(", ", orderClauses);
    }

    public <T> Page<T> getListPagination(String sql, Map<String, Object> params,
                                        com.example.java_template.common.persistence.dto.PageDto pageDto,
                                        Class<T> clazz) {
        return getListPagination(sql, params, pageDto, clazz, null);
    }

    /**
     * <p>Câu SQL truyền vào KHÔNG được tự mang {@code ORDER BY} — hàm này tự nối, nếu có sẵn
     * thành hai mệnh đề {@code ORDER BY} liền nhau và lỗi cú pháp.</p>
     *
     * <p>Thứ tự mặc định khai qua {@code defaultOrderBy} (vd {@code "id DESC"}), dùng khi client
     * không gửi sort; thiếu nó thì phân trang không ổn định, trang 2 có thể lặp lại bản ghi
     * của trang 1.</p>
     */
    public <T> Page<T> getListPagination(String sql, Map<String, Object> params,
                                        com.example.java_template.common.persistence.dto.PageDto pageDto,
                                        Class<T> clazz, String defaultOrderBy) {
        PageRequest pageable = getPage(pageDto);
        String orderBy = buildSafeOrder(pageDto.getSort(), pageDto.getDirection(), pageDto.getSorts(), clazz);
        if (StringUtils.isBlank(orderBy)) {
            orderBy = defaultOrderBy;
        }

        String pagedSql = sql;
        if (StringUtils.isNotBlank(orderBy)) {
            pagedSql += " ORDER BY " + orderBy;
        }
        pagedSql += String.format(" LIMIT %d OFFSET %d", pageable.getPageSize(), pageable.getOffset());

        long total = getTotalRow(sql, params);
        List<T> rows = namedParameterJdbcTemplate.query(pagedSql, params, new BeanPropertyRowMapper<>(clazz));
        return new PageImpl<>(rows, pageable, total);
    }

    public <T> List<T> getResultList(String sql, MapSqlParameterSource params, Class<T> clazz) {
        return namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(clazz));
    }

    public <T> T getFirstResult(String sql, MapSqlParameterSource params, Class<T> clazz) {
        List<T> result = namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(clazz));
        if (result.isEmpty()) {
            throw new com.example.java_template.common.exception.ResourceNotFoundException(clazz.getSimpleName());
        }
        return result.get(0);
    }

    public Long getTotalRow(String sql, Map<String, Object> params) {
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS sub";
        return namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
    }

    public int updateList(String sql, Map<String, Object> params) {
        return namedParameterJdbcTemplate.update(sql, params);
    }

    // ===== Batch =====

    public void batchInsert(String tableName, List<String> columns, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty() || columns == null || columns.isEmpty()) {
            return;
        }
        String cols = String.join(", ", columns);
        String placeholders = columns.stream().map(c -> ":" + c).collect(Collectors.joining(", "));
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, cols, placeholders);

        Map<String, Object>[] batchArgs = rows.toArray(new Map[0]);
        namedParameterJdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * Batch Upsert cho PostgreSQL: giúp tránh quá nhiều dòng trùng nhau khi người dùng
     * tick/bỏ tick liên tục. Thay vì tạo dòng mới, sẽ chuyển {@code is_deleted} về {@code 0}.
     *
     * <p>Ưu điểm: giảm tải DB, tăng tốc độ xử lý.</p>
     * <p>Nhược điểm: khó theo dõi audit nếu không có hệ thống log chuẩn chỉnh.</p>
     */
    public void batchUpsert(String tableName, List<String> columns, List<String> conflictColumns,
                            List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty() || columns == null || columns.isEmpty()
                || conflictColumns == null || conflictColumns.isEmpty()) {
            return;
        }

        String cols = String.join(", ", columns);
        String placeholders = columns.stream().map(c -> ":" + c).collect(Collectors.joining(", "));
        String conflictCols = String.join(", ", conflictColumns);
        String updateSet = columns.stream()
                .filter(c -> !conflictColumns.contains(c))
                .map(c -> c + " = EXCLUDED." + c)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (%s) DO UPDATE SET %s",
                tableName, cols, placeholders, conflictCols, updateSet);

        Map<String, Object>[] batchArgs = rows.toArray(new Map[0]);
        namedParameterJdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
