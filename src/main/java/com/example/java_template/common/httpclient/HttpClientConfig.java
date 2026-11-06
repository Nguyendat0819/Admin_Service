package com.example.java_template.common.httpclient;

import com.example.java_template.common.util.SecurityUtil;
import io.micrometer.observation.ObservationRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "internal", basePackages = "com.example.java_template.feature.client")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HttpClientConfig {

    InternalProperties internalProperties;

    @Bean
    public RestClientHttpServiceGroupConfigurer httpServiceGroupConfigurer(
            ObservationRegistry observationRegistry) {
        return groups -> groups.forEachClient((group, builder) -> {
            builder.observationRegistry(observationRegistry);
            builder.requestInterceptor(authInterceptor());
            String baseUrl = switch (group.name()) {
                case "internal" -> internalProperties.getBaseUrl();
                default -> null;
            };
            if (StringUtils.isNotBlank(baseUrl)) {
                builder.baseUrl(baseUrl);
            }
        });
    }

    private ClientHttpRequestInterceptor authInterceptor() {
        return (request, body, execution) -> {
            String token = SecurityUtil.getTokenValue();
            if (StringUtils.isNotBlank(token)) {
                request.getHeaders().setBearerAuth(token);
            }
            return execution.execute(request, body);
        };
    }
}
