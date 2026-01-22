package com.back.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.member.dto.*;
import com.back.domain.member.service.MemberService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> join(@RequestBody JoinRequest req) {
        JoinResponse res = memberService.join(req);
        return ResponseEntity.ok(res);
    }

    // 로그인 (JWT 없음)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse res = memberService.login(req, response);
        return ResponseEntity.ok(res);
    }

    // 보호 API: 토큰 있어야만 접근 가능하게 만들 거임
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(memberService.me());
    }

    // 나중에 apikey 리프레쉬토큰 도입하면 그때 delete 로 할지 고민중
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", memberService.buildLogoutSetCookieHeader());
        return ResponseEntity.ok().build();
    }
}
