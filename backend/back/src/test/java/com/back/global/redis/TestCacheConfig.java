package com.back.global.redis;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.TestPropertySource;

@TestConfiguration
@EnableCaching
// 이 테스트 컨텍스트 내에서만 설정을 redis(또는 simple)로 덮어씁니다.
@TestPropertySource(properties = "spring.cache.type=redis")
public class TestCacheConfig {
    // 필요하다면 여기서 RedisCacheManager 빈을 직접 정의할 수도 있습니다.
}
