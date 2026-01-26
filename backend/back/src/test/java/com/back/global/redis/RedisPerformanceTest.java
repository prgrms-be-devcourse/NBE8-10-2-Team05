package com.back.global.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableCaching
public class RedisPerformanceTest {

    @Autowired
    private RedisExampleCustomRepository realRedisExampleCustomRepository;

    @Autowired
    private RedisService redisService;

    @Test
    @Transactional
    @DisplayName("DB 조회와 Redis 캐시 조회 속도 비교")
    void comparePerformance() {
        Integer redisId = 1;
        realRedisExampleCustomRepository.save(new RedisCustomEntity(1, "nick", "key"));

        // 1. 첫 번째 조회 (Cache Miss - DB 접근)
        long startTime = System.currentTimeMillis();
        RedisEntity result = redisService.getUser(redisId);
        long dbTime = System.currentTimeMillis() - startTime;

        // 2. 두 번째 조회 (Cache Hit - Redis 접근)
        startTime = System.currentTimeMillis();
        RedisEntity result2 = redisService.getUser(redisId);
        long redisTime = System.currentTimeMillis() - startTime;

        System.out.println("--------------------------------");
        System.out.println("DB 조회 시간: " + dbTime + "ms");
        System.out.println("Redis 조회 시간: " + redisTime + "ms");
        System.out.println("성능 개선: " + (double) dbTime / redisTime + "배");
        System.out.println("--------------------------------");

        assertThat(redisTime).isLessThan(dbTime);
    }
}
