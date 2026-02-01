package com.back.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.auth.service.AuthServiceRedis;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthServiceRedis authService;

    // 리프레시토큰 쿠키 검증해서 새 쿠키 내려주기
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        // TODO: header authorization과 cookie를 같이 사용하는 것으로 알고 있습니다.
        //      cookie만 설정하면 되는 건가요?
        response.addHeader("Set-Cookie", authService.reissueAccessTokenCookie(request));
        return ResponseEntity.ok().build();
    }
}
