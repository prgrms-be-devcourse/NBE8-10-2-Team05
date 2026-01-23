package com.back.domain.auth.util;

import java.util.UUID;

/**
 * Refresh Token 원문(클라이언트에게 줄 값)을 생성하는 클래스
 *
 * Refresh Token은 JWT로 만들지 않고 UUID 같은 "랜덤 문자열"로 만든다.
 * - 이유: 서버(DB/Redis)에 저장해두고, 삭제하면 즉시 무효화(진짜 로그아웃) 가능
 */
public class RefreshTokenGenerator {

    private RefreshTokenGenerator() {
        // 유틸 클래스라 new 못 하게 막음
    }

    /**
     * Refresh Token "원문" 생성
     * - 예: "550e8400-e29b-41d4-a716-446655440000"
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
