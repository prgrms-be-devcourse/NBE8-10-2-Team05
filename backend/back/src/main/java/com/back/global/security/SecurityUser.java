package com.back.global.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

public class SecurityUser extends User {
    @Getter
    private final int id;

    @Getter
    private final String name;

    public SecurityUser(
            int id, String username, String password, String name, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.name = name;
    }
}
