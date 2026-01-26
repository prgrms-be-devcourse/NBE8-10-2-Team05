package com.back.global.redis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class RedisServiceTest {
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setup() {
        RedisEntity testEntity = RedisEntity.builder()
                .id("1")
                .nickname("redis")
                .apiKey("secret-key")
                .build();

        Cache cache = cacheManager.getCache("redis");
        if (cache != null) cache.clear();
    }

    @Test
    @DisplayName("redis 에 정보가 잘 올라가는지 확인")
    void getUser() {}

    @Test
    @DisplayName("redis 에 올라간 정보가 잘 수정되는지 확인")
    void updateUser() {}

    @Test
    @DisplayName("redis 에 올라간 정보가 잘 삭제되는지 확인 ")
    void deleteUser() {}
}
