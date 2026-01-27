package com.back.global.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.security.SecurityUser;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final Environment env;
    private final MemberService memberService;

    // 토큰 없이도 허용할 경로들 (SecurityConfig의 permitAll과 맞춰주는 게 중요)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/h2-console")
                || path.equals("/api/v1/member/member/join")
                || path.equals("/api/v1/member/member/login")
                || path.equals("/api/v1/member/member/logout")
                || path.equals("/api/v1/auth/reissue")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 테스트에서는 인증 로직을 아예 건너뛰고 전부 통과
        if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 추출: Authorization 우선, 없으면 쿠키
        String token = resolveToken(request);

        // String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1) Authorization 헤더가 없으면 그냥 다음으로
        // (단, 보호된 경로는 결국 Security에서 401 처리됨)
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        //        // 2) Bearer 형식 검사
        //        if (!authHeader.startsWith("Bearer ")) {
        //            throw new ServiceException("AUTH-401", "Authorization 헤더가 Bearer 형식이 아닙니다.");
        //        }
        //
        //        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            // 3) 토큰 검증 + Claims 추출
            Claims claims = jwtProvider.getClaims(token);
            //            System.out.println("JWT subject = " + claims.getSubject());
            //            System.out.println("JWT role = " + claims.get("role"));

            // 4) Claims에서 필요한 정보 추출
            Long memberId = Long.valueOf(claims.getSubject());
            String role = String.valueOf(claims.get("role")); // 예: USER

            // 5) Spring Security 인증 객체 생성
            // 5) ✅ DB에서 Member를 조회해서 SecurityUser를 "완성"시킨다
            //    - 그래야 username을 buildUsername(member) 규칙으로 통일 가능
            Member member = memberService
                    .findById(memberId)
                    .orElseThrow(() -> new ServiceException("AUTH-401", "존재하지 않는 회원입니다."));

            // 6) ✅ 권한(authorities) 구성
            //    member.role(USER/ADMIN) → "ROLE_USER" 형태로 변환
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));

            // 7) ✅ principal을 SecurityUser로 통일 (강사님 요구 핵심)
            //    - username: "로그인 식별자" 규칙으로 통일
            //    - name: 화면용 이름
            SecurityUser user = new SecurityUser(
                    member.getId(),
                    buildUsername(member),
                    "", // JWT 인증에서는 credentials 의미 없음 (빈값/NULL 가능)
                    member.getName(),
                    authorities);

            // 8) ✅ Authentication 생성 후 SecurityContext에 저장
            //    이 시점부터 Spring Security는 "인증된 사용자 요청"으로 인식함
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());

            // 6) SecurityContext에 저장 → 이후 컨트롤러에서 인증된 사용자로 인식됨
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 토큰이 위조되었거나, 만료되었거나, 파싱 실패한 경우
            throw new ServiceException("AUTH-401", "유효하지 않은 토큰입니다.");
        }
    }

    /**
     * ✅ username을 “로그인 식별자”로 통일
     * - EMAIL: email
     * - 소셜: {type}__{providerId} (ex. KAKAO__123456)
     *
     * 이렇게 해두면 OAuth2에서도 강사님이 쓰던 "KAKAO__{oauthUserId}"와 완전히 같은 철학으로 맞춰짐.
     */
    private String buildUsername(Member member) {
        if (member.getType() == Member.LoginType.EMAIL) {
            // 이메일 회원은 email이 로그인 식별자
            return member.getEmail();
        }

        // 소셜 회원은 (type, providerId)가 식별자
        // providerId는 null이면 안 됨 (DB 설계상 소셜은 providerId 있어야 함)
        return member.getType().name() + "__" + member.getProviderId();
    }

    // Authorization: Bearer 토큰이 있으면 그걸 사용하고, 없으면 HttpOnly 쿠키(accessToken)에서 읽는다.
    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 헤더가 있으면 Bearer 형식만 허용 (기존 정책 유지)
        if (authHeader != null && !authHeader.isBlank()) {
            if (!authHeader.startsWith("Bearer ")) {
                throw new ServiceException("AUTH-401", "Authorization 헤더가 Bearer 형식이 아닙니다.");
            }
            return authHeader.substring("Bearer ".length()).trim();
        }

        // 헤더가 없으면 쿠키에서 accessToken 읽기
        return getCookieValue(request, "accessToken");
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                String value = cookie.getValue();
                return (value == null || value.isBlank()) ? null : value;
            }
        }
        return null;
    }
}
