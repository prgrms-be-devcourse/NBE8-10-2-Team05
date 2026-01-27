package com.back.global.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Getter;

public class SecurityUser extends User implements OAuth2User {
    @Getter
    private final Long id;

    @Getter
    private final String name;

    public SecurityUser(
            Long id,
            String username,
            String password,
            String name,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.name = name;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }
}
