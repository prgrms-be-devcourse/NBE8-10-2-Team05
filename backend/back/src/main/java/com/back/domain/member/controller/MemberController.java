package com.back.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.member.dto.JoinRequest;
import com.back.domain.member.dto.JoinResponse;
import com.back.domain.member.dto.LoginRequest;
import com.back.domain.member.dto.LoginResponse;
import com.back.domain.member.service.MemberService;

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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse res = memberService.login(req);
        return ResponseEntity.ok(res);
    }
}
