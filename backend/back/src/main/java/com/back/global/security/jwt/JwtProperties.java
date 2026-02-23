package com.back.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom")
public record JwtProperties(Jwt jwt, AccessToken accessToken) {
    public record Jwt(
            // JWT 서명 비밀키
            String secretKey) {}

    public record AccessToken(
            // Access Token 만료 시간 (초 단위)
            long expirationSeconds) {}
}
