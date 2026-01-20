package com.back.global.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.back.global.exception.ServiceException;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // 토큰 없이도 허용할 경로들 (SecurityConfig의 permitAll과 맞춰주는 게 중요)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/h2-console")
                || path.equals("/api/v1/member/join")
                || path.equals("/api/v1/member/login")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1) Authorization 헤더가 없으면 그냥 다음으로
        // (단, 보호된 경로는 결국 Security에서 401 처리됨)
        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) Bearer 형식 검사
        if (!authHeader.startsWith("Bearer ")) {
            throw new ServiceException("AUTH-401", "Authorization 헤더가 Bearer 형식이 아닙니다.");
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            // 3) 토큰 검증 + Claims 추출
            Claims claims = jwtProvider.getClaims(token);

            // 4) Claims에서 필요한 정보 추출
            Long memberId = Long.valueOf(claims.getSubject());
            String role = String.valueOf(claims.get("role")); // 예: USER

            // 5) Spring Security 인증 객체 생성
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            var authentication = new UsernamePasswordAuthenticationToken(
                    memberId, // principal (간단히 memberId 넣음)
                    null,
                    authorities);

            // 6) SecurityContext에 저장 → 이후 컨트롤러에서 인증된 사용자로 인식됨
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 토큰이 위조되었거나, 만료되었거나, 파싱 실패한 경우
            throw new ServiceException("AUTH-401", "유효하지 않은 토큰입니다.");
        }
    }
}
