package com.example.java_template.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static com.example.java_template.common.util.Const.ROLE_PREFIX;
import static com.example.java_template.common.util.SecurityUtil.getRolesFromJwt;

/**
 * Chuyển {@link Jwt} của Keycloak thành {@link AbstractAuthenticationToken}: đọc roles +
 * permission theo role (qua {@link PermissionResolver}) rồi gắn vào {@code authorities}.
 *
 * <p>Đây là điểm QUYẾT ĐỊNH user có những quyền gì — chạy 1 lần mỗi request tại filter,
 * sau đó {@code @PreAuthorize} chỉ đọc lại để so khớp.</p>
 */
@Component
@RequiredArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PermissionResolver permissionResolver;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // roles
        Set<String> roles = getRolesFromJwt(jwt);
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));

        // permissions (look-up từ role qua cache)
        permissionResolver.permissionsOf(roles)
                .forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
