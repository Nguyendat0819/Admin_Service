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
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Tự đăng ký các interface {@code @HttpExchange} trong package
 * {@code com.example.java_template.feature.client} thành proxy bean, GỘP theo base URL.
 *
 * <ul>
 *   <li>1 group = 1 base URL. Client chung URL -> để chung sub-package -> cùng group.</li>
 *   <li>Mở rộng thêm service khác: thêm {@code @ImportHttpServices(group="external", basePackages="...")}
 *       + 1 nhánh {@code case "external"} trong {@code switch} dưới.</li>
 * </ul>
 *
 * <p>Cấu hình chung (token + traceId) khai 1 lần; base URL set RIÊNG theo tên group.</p>
 */
@Configuration
@ImportHttpServices(group = "internal", basePackages = "com.example.java_template.feature.client")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HttpClientConfig {

    static String INTERNAL_REGISTRATION_ID = "internal";

    InternalProperties internalProperties;

    @Bean
    public RestClientHttpServiceGroupConfigurer httpServiceGroupConfigurer(
            ObservationRegistry observationRegistry,
            OAuth2AuthorizedClientManager authorizedClientManager) {
        return groups -> groups.forEachClient((group, builder) -> {
            builder.observationRegistry(observationRegistry);                       // propagate traceId
            builder.requestInterceptor(authInterceptor(authorizedClientManager));   // gắn Bearer token
            String baseUrl = switch (group.name()) {
                case "internal" -> internalProperties.getBaseUrl();
                // case "external" -> externalProperties.getBaseUrl();
                default -> null;
            };
            if (StringUtils.isNotBlank(baseUrl)) {
                builder.baseUrl(baseUrl);
            }
        });
    }

    /** Manager lấy token {@code client_credentials} (luồng KHÔNG có user: job/kafka/startup). */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    /**
     * Gắn Bearer vào mỗi request ra ngoài: ưu tiên token user (request do người kích hoạt)
     * giữ đúng danh tính; không có -> token {@code client_credentials} của service
     * (vd khi reload cache ở {@code @PostConstruct} / job).
     */
    private ClientHttpRequestInterceptor authInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        return (request, body, execution) -> {
            String token = SecurityUtil.getTokenValue();
            if (StringUtils.isBlank(token)) {
                token = clientCredentialsToken(authorizedClientManager);
            }
            if (StringUtils.isNotBlank(token)) {
                request.getHeaders().setBearerAuth(token);
            }
            return execution.execute(request, body);
        };
    }

    private String clientCredentialsToken(OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(INTERNAL_REGISTRATION_ID)
                .principal(INTERNAL_REGISTRATION_ID)
                .build();
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);
        return client != null ? client.getAccessToken().getTokenValue() : null;
    }
}
