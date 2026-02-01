package com.back.domain.auth.service;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.auth.store.RedisRefreshTokenStore;
import com.back.domain.auth.util.TokenHasher;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceRedis {

    /**
     * ✅ Redis 기반 리프레시 토큰 저장소
     * - key: rt:{tokenHash}
     * - value: memberId
     * - TTL: 만료시간
     */
    private final RedisRefreshTokenStore redisRefreshTokenStore;

    /**
     * ✅ JwtProvider는 accessToken 발급 시 email/role 클레임이 필요함
     * 그래서 Redis에서 memberId를 찾은 다음, Member를 DB에서 1번 조회한다.
     */
    private final MemberRepository memberRepository;

    private final JwtProvider jwtProvider;

    // access token 쿠키 만료 시간 (기존 코드 유지)
    private static final Duration ACCESS_MAX_AGE = Duration.ofMinutes(20);

    /**
     * refreshToken 쿠키를 검증해서 새 accessToken 쿠키(헤더 문자열)를 반환한다.
     *
     * [기존(DB)]
     * - refreshToken hash로 refresh_token 테이블 조회
     * - revoked/expired 확인
     * - member 꺼내서 accessToken 발급
     *
     * [변경(Redis)]
     * - refreshToken hash로 Redis에서 memberId 조회
     * - 없으면(키 없음) => 만료됐거나 폐기된 토큰
     * - memberId로 Member DB 조회 후 accessToken 발급
     */
    @Transactional(readOnly = true)
    public String reissueAccessTokenCookie(HttpServletRequest request) {

        // 1) 요청 쿠키에서 refreshToken 꺼내기 (기존 코드 그대로)
        String rawRefreshToken = getCookieValue(request, "refreshToken");
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ServiceException("AUTH-401", "refreshToken 쿠키가 없습니다.");
        }

        // TODO: 유효성 검증 로직이 빠진 것 같습니다.
        //       현재, 우리 redis에 토큰이 있는지만 검사하고 있습니다.
        //       refreshToken자체의 검증로직이 필요합니다.
        //       유효하지 않은 가짜 토큰이라면, 취소.
        //       유효하지 않고 유통기한이 지난 토큰이라면 재발급.

        // TODO: isActive 있던데 안쓰셨더라구요.
        boolean valid = true;
        boolean isExpired = true;
        if (valid && isExpired) {
            // System.out.println("이때만 재발급");
        }

        // 2) refreshToken 원문을 SHA-256 해시로 변환 (기존 그대로)
        String tokenHash = TokenHasher.sha256Hex(rawRefreshToken);

        // 3) Redis에서 memberId 조회
        // - Redis에 key가 없으면: (1) 만료(TTL 끝) (2) 로그아웃/회전으로 delete된 토큰
        Long memberId = redisRefreshTokenStore
                .findMemberId(tokenHash)
                .orElseThrow(() -> new ServiceException("AUTH-401", "유효하지 않은 refresh token 입니다."));

        // TODO: id를 가져오면서 role,email도 가져오게 하면 따로 db조회가 필요없을 것 같습니다.
        //      email은 아예 빼도 되셔도 될 것 같습니다.

        // 4) accessToken 발급에 email/role이 필요하므로 Member 조회
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 회원입니다."));

        // 5) JwtProvider로 새 accessToken 발급
        String newAccessToken =
                jwtProvider.issueAccessToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        // TODO: refreshToken 자체의 재발급 로직도 필요합니다.

        // 6) 새 access 토큰 쿠키 헤더 문자열 반환
        return buildAccessCookieHeader(newAccessToken);
    }

    /**
     * 요청에서 쿠키 값을 꺼내는 유틸
     * - name에 해당하는 쿠키가 없으면 null 반환
     */
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

    // TODO: 맨 처음 accessToken 발급할 때도 이런 설정이 있었나요?
    /**
     * accessToken을 HttpOnly 쿠키로 내려주는 Set-Cookie 헤더 문자열 생성
     */
    private String buildAccessCookieHeader(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // dev 환경(http)이면 false / prod(https)이면 true
                .path("/")
                .sameSite("Lax")
                .maxAge(ACCESS_MAX_AGE)
                .build()
                .toString();
    }
}
