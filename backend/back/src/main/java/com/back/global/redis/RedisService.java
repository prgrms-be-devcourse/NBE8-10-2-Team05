package com.back.global.redis;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisRepository redisRepository;

    /**
     * Redis에는 user:1 키-데이터로 저장됩니다.
     * Redis에 값이 있으면 메서드를 실행하지 않고 바로 반환합니다
     * Redis에 값이 없으면 메서드를 실행하고, 그 결과를 Redis에 저장합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "redis", key = "#redisId")
    public RedisEntity getUser(Integer redisId) {
        // Redis에 데이터가 없으면 이 로직이 실행되어 DB를 조회합니다.
        return redisRepository.findById(redisId).orElseThrow();
    }

    /**
     * @Cacheable과 달리 메서드를 항상 실행하며, 실행 결과를 Redis에 덮어씁니다.
     *
     */
    @Transactional
    @CachePut(value = "redis", key = "#redisId")
    public RedisEntity updateUser(Integer userId) {
        // DB 데이터를 수정하고
        RedisEntity redisEntity = redisRepository.findById(userId).orElseThrow();
        // redisEntity.update();

        // 수정된 객체를 반환하면 Redis의 기존 캐시가 이 값으로 교체됩니다.
        return redisEntity;
    }

    @Transactional
    @CacheEvict(value = "redis", key = "#redisId")
    public void deleteUser(Integer userId) {
        // DB에서 삭제
        redisRepository.deleteById(userId);
        // 메서드가 성공적으로 끝나면 Redis에 있던 "user::userId" 키도 삭제됩니다.
    }
}
