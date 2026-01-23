package com.back.domain.auth.util;

import java.util.UUID;

public class RefreshTokenGenerator {

    private RefreshTokenGenerator() {
        // 유틸 클래스라 new 못 하게 막음
    }

    // 리프레시 토큰 uuid 생성
    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
