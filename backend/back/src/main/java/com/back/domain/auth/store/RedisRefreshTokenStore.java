package com.back.domain.auth.store;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Redis에 Refresh Token을 저장/조회/삭제하는 전용 저장소
 *
 * 기존 DB의 RefreshTokenRepository 역할을 대신함
 *
 * Redis 저장 구조:
 *  key   = "rt:{tokenHash}"
 *  value = memberId
 *  TTL   = refresh token 만료 시간
 */
@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenStore {

    // Redis에 문자열(key-value) 형태로 접근하기 위한 템플릿
    private final StringRedisTemplate redis;

    // Redis key prefix (refresh token 구분용)
    private static final String PREFIX = "rt:";

    /**
     * Refresh Token 저장
     *
     * @param tokenHash refresh token 원문을 해시한 값
     * @param memberId  어떤 회원의 토큰인지
     * @param ttl       만료 시간 (ex: 14일)
     */
    public void save(String tokenHash, Long memberId, Duration ttl) {
        // 실제 Redis에 저장되는 key 예: rt:abc123hash
        String key = PREFIX + tokenHash;

        // value는 memberId (문자열로 저장)
        redis.opsForValue().set(key, String.valueOf(memberId), ttl);
        // TTL이 끝나면 Redis가 자동으로 삭제해줌
    }

    /**
     * Refresh Token으로 memberId 조회
     *
     * @param tokenHash refresh token 해시값
     * @return memberId (없으면 Optional.empty())
     */
    public Optional<Long> findMemberId(String tokenHash) {
        String key = PREFIX + tokenHash;

        // Redis에서 value 조회
        String value = redis.opsForValue().get(key);

        // 없으면 유효하지 않은 refresh token
        if (value == null) {
            return Optional.empty();
        }

        // 있으면 memberId 반환
        return Optional.of(Long.valueOf(value));
    }

    /**
     * Refresh Token 삭제 (로그아웃 / 토큰 폐기 시 사용)
     *
     * @param tokenHash refresh token 해시값
     */
    public void delete(String tokenHash) {
        String key = PREFIX + tokenHash;
        redis.delete(key);
    }
}
