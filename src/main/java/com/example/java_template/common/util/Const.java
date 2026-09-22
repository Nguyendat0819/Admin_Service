package com.example.java_template.common.util;

import java.time.format.DateTimeFormatter;

/**
 * Hằng số dùng chung toàn dự án — gom về 1 chỗ để tránh hard-code ở nhiều nơi.
 */
public final class Const {

    private Const() {
    }

    // ---- JWT / Security claims ----
    public static final String REALM_ACCESS = "realm_access";
    public static final String RESOURCE_ACCESS = "resource_access";
    public static final String ROLES = "roles";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String PREFERRED_USERNAME = "preferred_username";
    public static final String ANONYMOUS = "anonymous";
    public static final String ADMIN = "ADMIN";

    // ---- Logging / MDC ----
    public static final String REQUEST_ID = "requestId";
    public static final String USERNAME = "username";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    // ---- Pagination ----
    public static final int PAGE_DEFAULT = 0;
    public static final int SIZE_DEFAULT = 15;
    public static final int SIZE_MAX = 200;

    // ---- File ----
    public static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
}
