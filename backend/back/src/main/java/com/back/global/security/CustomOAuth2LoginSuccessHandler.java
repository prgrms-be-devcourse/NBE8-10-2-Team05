package com.back.global.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.security.jwt.JwtProvider;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long memberId = oAuth2User.getAttribute("memberId");

        Member member = memberService.findById(memberId).orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));

        // 일반 로그인과 동일한 쿠키 발급 (access + refresh)
        memberService.issueLoginCookies(member, response);

        // PRE_REGISTERED 상태면 추가정보 입력 페이지로, ACTIVE면 메인으로
        if (member.getStatus() == Member.MemberStatus.PRE_REGISTERED) {
            response.sendRedirect("http://localhost:3000/social-signup");
        } else {
            response.sendRedirect("http://localhost:3000");
        }
    }
}
