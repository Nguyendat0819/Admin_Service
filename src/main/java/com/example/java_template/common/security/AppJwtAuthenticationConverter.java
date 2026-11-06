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

@Component
@RequiredArgsConstructor
public class AppJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PermissionResolver permissionResolver;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Set<String> roles = getRolesFromJwt(jwt);

        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));
        permissionResolver.permissionsOf(roles)
                .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
