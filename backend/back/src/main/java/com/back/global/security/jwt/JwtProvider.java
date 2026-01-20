package com.back.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpSeconds;

    public JwtProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.jwt().secretKey().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpSeconds = props.accessToken().expirationSeconds();
    }

    public String issueAccessToken(long memberId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenExpSeconds);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", email)
                .claim("role", role)
                .signWith(key)
                .compact();
    }
}
