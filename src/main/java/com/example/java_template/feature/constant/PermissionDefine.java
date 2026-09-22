package com.example.java_template.feature.constant;

/**
 * Định nghĩa permission code cho từng action nghiệp vụ.
 *
 * <p>Quy tắc đặt tên: {@code <tính-năng>:<đối-tượng>:<action>}. Tên cũng chính là
 * giá trị truyền cho annotation {@code @RequiresPermission}.</p>
 *
 * <p>Ví dụ:</p>
 * <pre>
 *   &#64;RequiresPermission(PermissionDefine.TEMPLATE_TEST)
 * </pre>
 */
public final class PermissionDefine {

    private PermissionDefine() {
    }

    // TemplateApi (mẫu)
    public static final String TEMPLATE_TEST = "template:template:test";
}
