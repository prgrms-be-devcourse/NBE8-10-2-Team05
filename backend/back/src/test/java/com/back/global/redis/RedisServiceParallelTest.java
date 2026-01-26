package com.back.global.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableCaching
class RedisServiceParallelTest {
    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private RedisExampleCustomRepository redisExampleCustomRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private RedisCustomEntity testEntity;

    @BeforeEach
    void setup() {
        testEntity = RedisCustomEntity.builder()
                .id(1)
                .nickname("redis")
                .apiKey("secret-key")
                .build();

        Cache cache = cacheManager.getCache("redis");
        if (cache != null) cache.clear();
        reset(redisExampleCustomRepository);
    }

    @AfterAll
    static void clearAllCache(@Autowired CacheManager cacheManager) {
        Cache cache = cacheManager.getCache("redis");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("getUser 확인")
    void getUser() throws InterruptedException {
        Integer redisId = 1;
        when(redisExampleCustomRepository.findById(redisId)).thenReturn(Optional.of(testEntity));

        // 첫 번째 호출 (Cache Miss -> DB 접근)
        RedisEntity result = redisService.getUser(redisId);

        // 두 번째 호출 (Cache Hit -> DB 접근 안 함)
        RedisEntity result2 = redisService.getUser(redisId);

        assertThat(result2.id()).isEqualTo(testEntity.id());
        assertThat(result2.nickname()).isEqualTo(testEntity.nickname());
        verify(redisExampleCustomRepository, times(1)).findById(redisId); // DB 조회가 딱 1번만 일어났는지 검증

        // cacheManager를 통해 redis를 사용하기 때문에
        Cache.ValueWrapper cacheContent = cacheManager.getCache("redis").get(redisId);
        assertThat(cacheContent).isNotNull();
    }

    @Test
    @DisplayName("deleteUser 확인 ")
    void deleteUser() {
        Integer redisId = 1;
        String physicalKey = "redis::" + redisId;

        // Mock이므로 실제 데이터 유무와 상관없이 호출은 성공함
        when(redisExampleCustomRepository.findById(redisId)).thenReturn(Optional.of(testEntity));
        doNothing().when(redisExampleCustomRepository).deleteById(redisId);

        // service호출 대신 실제 cache사용
        // service에 @Tramsactional 붙어있으면 테스트가 어렵기 때문
        // Cache cache = cacheManager.getCache("redis");
        // cache.put(redisId, RedisEntity.from(testEntity));
        redisService.getUser(redisId);

        assertThat(redisTemplate.hasKey(physicalKey)).as("캐시 주입 성공").isTrue();

        redisService.deleteUser(redisId);

        assertThat(redisTemplate.hasKey(physicalKey)).as("캐시 삭제 실패").isFalse();
    }

    @Test
    @DisplayName("멀티코어 자원공유 문제시 무조건 충돌나는 테스트")
    void forceFailureTest() throws InterruptedException {
        String physicalKey = "redis::" + 1;

        if (redisTemplate.hasKey(physicalKey)) {
            throw new RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.");
        }

        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));
        // redis::1 키가 없네? 내가 만들고 1초 동안 잘게." (Sleep 시작)
        // 충돌 발생 (deleteUser 시점)
        Thread.sleep(1000);

        redisTemplate.delete(physicalKey);
    }
}
