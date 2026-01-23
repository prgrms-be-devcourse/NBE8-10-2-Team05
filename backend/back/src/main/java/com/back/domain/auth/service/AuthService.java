package com.back.domain.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.auth.entity.RefreshToken;
import com.back.domain.auth.repository.RefreshTokenRepository;
import com.back.domain.auth.util.TokenHasher;
import com.back.domain.member.entity.Member;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    private static final Duration ACCESS_MAX_AGE = Duration.ofMinutes(20);

    // refreshToken 쿠키를 검증해서 새 accessToken 쿠키 반환
    @Transactional(readOnly = true)
    public String reissueAccessTokenCookie(HttpServletRequest request) {
        // 요청에서 리프레시 토큰 꺼내기
        String rawRefreshToken = getCookieValue(request, "refreshToken");
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ServiceException("AUTH-401", "refreshToken 쿠키가 없습니다.");
        }

        // 해시로 변환
        String hash = TokenHasher.sha256Hex(rawRefreshToken);

        // 해시로 디비 조회
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> new ServiceException("AUTH-401", "유효하지 않은 refresh token 입니다."));

        LocalDateTime now = LocalDateTime.now();
        if (refreshToken.isRevoked()) {
            throw new ServiceException("AUTH-401", "폐기된 refresh token 입니다.");
        }
        if (refreshToken.isExpired()) {
            throw new ServiceException("AUTH-401", "만료된 refresh token 입니다.");
        }

        // 회원 정보 꺼내서 새 AccessToken 발급
        Member member = refreshToken.getMember();
        String newAccessToken =
                jwtProvider.issueAccessToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        // 새 access 토큰 반환
        return buildAccessCookieHeader(newAccessToken);
    }

    // 요청에서 리프레시 토큰 꺼내기
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    // 로그인이랑 같은 토큰 생성
    private String buildAccessCookieHeader(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // dev
                .path("/")
                .sameSite("Lax")
                .maxAge(ACCESS_MAX_AGE)
                .build()
                .toString();
    }
}
