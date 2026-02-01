package com.back.domain.member.member.service;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    public String accessCookie(String token, Duration maxAge) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // TODO: 추후 true로 변경
                .path("/")
                .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
                .maxAge(maxAge)
                .build()
                .toString();
    }

    public String refreshCookie(String raw, Duration maxAge) {
        return ResponseCookie.from("refreshToken", raw)
                .httpOnly(true)
                .secure(false) // TODO: 추후 true로 변경
                .path("/") // TODO: refreshToken의 경로는 재발급을 담당하는 API 주소(예: /api/v1/auth/reissue)로 한정하는 것이 정석
                .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
                .maxAge(maxAge)
                .build()
                .toString();
    }

    public String deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false) // TODO: 추후 true로 변경
                .path("/")
                .sameSite("Lax") // TODO: 추후 배포시에는 None + secure(true)로 바꿔야 할 수 있음
                .maxAge(Duration.ZERO)
                .build()
                .toString();
    }
}
