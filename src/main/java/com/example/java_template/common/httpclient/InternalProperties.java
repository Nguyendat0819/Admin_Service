package com.example.java_template.common.httpclient;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bind các property dưới prefix {@code app.internal.*} vào Java để {@link HttpClientConfig}
 * dùng cho {@code base-url} của group internal.
 *
 * <p>Ví dụ:</p>
 * <pre>
 * app:
 *   internal:
 *     base-url: https://api.platform.example.com
 *     enabled: true
 *     refresh-ms: 300000
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.internal")
public class InternalProperties {

    private String baseUrl;
    private boolean enabled = true;
    private long refreshMs = 300_000L;
}
