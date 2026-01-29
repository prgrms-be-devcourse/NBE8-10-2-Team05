package com.back.global.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
class RedisServiceTest {
    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private RedisExampleCustomRepository redisExampleCustomRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String redisName = "redis";

    private RedisCustomEntity createTestEntity(Integer id, String nickname, String apiKey) {
        return RedisCustomEntity.builder()
                .id(id)
                .nickname(nickname)
                .apiKey(apiKey)
                .build();
    }

    private Integer generateUniqueId() {
        return Math.abs(UUID.randomUUID().hashCode());
    }

    private void cleanCache(Integer redisId) {
        Cache redisCache = cacheManager.getCache(redisName);
        if (redisCache == null) {
            return;
        }

        redisCache.evict(redisId);

        // 삭제 확인 (동기화 대기)
        String physicalKey = "redis::" + redisId;
        int retries = 0;
        while (Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey)) && retries < 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            retries++;
        }
    }

    @Test
    @DisplayName("Redis DB에 물리적으로 데이터가 저장되었는지 확인")
    void checkPhysicalRedis() throws InterruptedException {
        Integer redisId = generateUniqueId();
        RedisCustomEntity testEntity = createTestEntity(redisId, "redis", "apiKey");

        // 스프링 캐시가 생성하는 규칙에 따른 실제 키값 생성 (value::key)
        String expectedKey = "redis::" + redisId;

        // Redis에 데이터 저장
        redisTemplate.opsForValue().set(expectedKey, RedisEntity.from(testEntity));

        // 저장 완료 대기
        waitForCondition(() -> Boolean.TRUE.equals(redisTemplate.hasKey(expectedKey)), 2000, 50);

        // 데이터 조회
        Object actualValue = redisTemplate.opsForValue().get(expectedKey);

        // 검증
        assertThat(actualValue).isNotNull();
        assertThat(actualValue).isInstanceOf(RedisEntity.class); // 직렬화가 잘 되어 객체로 복원되는지 확인

        RedisEntity cachedEntity = (RedisEntity) actualValue;
        assertThat(cachedEntity.id()).isEqualTo(redisId);
        assertThat(cachedEntity.nickname()).isEqualTo("redis");

        // 삭제
        redisTemplate.delete(expectedKey);

        // 삭제 완료 대기
        waitForCondition(() -> Boolean.FALSE.equals(redisTemplate.hasKey(expectedKey)), 2000, 50);
    }

    @Test
    @DisplayName("getUser 확인")
    void getUser() throws InterruptedException {
        Mockito.reset(redisExampleCustomRepository);

        Integer redisId = generateUniqueId();
        String physicalKey = "redis::" + redisId;
        RedisCustomEntity testEntity = createTestEntity(redisId, "nick", "apiKey");

        when(redisExampleCustomRepository.findById(redisId)).thenReturn(Optional.of(testEntity));

        // 첫 번째 호출 (Cache Miss -> DB 접근)
        RedisEntity result = redisService.getUser(redisId);

        // 캐시 생성 완료 대기
        waitForCondition(() -> Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey)), 2000, 50);

        // 두 번째 호출 (Cache Hit -> DB 접근 안 함)
        RedisEntity result2 = redisService.getUser(redisId);

        // 검증
        assertThat(result2.id()).isEqualTo(testEntity.id());
        assertThat(result2.nickname()).isEqualTo(testEntity.nickname());
        verify(redisExampleCustomRepository, times(1)).findById(redisId); // DB 조회가 딱 1번만 일어났는지 검증

        // 캐시 정리
        cleanCache(redisId);
    }

    @Test
    @DisplayName("updateUser 확인")
    void updateUser() throws InterruptedException {
        Integer redisId = generateUniqueId();
        String physicalKey = "redis::" + redisId;
        RedisCustomEntity oldEntity = createTestEntity(redisId, "nick", "oldApiKey");
        RedisCustomEntity newEntity = createTestEntity(redisId, "nick", "newApiKey");

        when(redisExampleCustomRepository.findById(redisId))
                .thenReturn(Optional.of(oldEntity))
                .thenReturn(Optional.of(newEntity));

        redisService.getUser(redisId);

        RedisEntity result = redisService.updateUser(redisId, "newApiKey");

        waitForCondition(
                () -> {
                    Object val = redisTemplate.opsForValue().get(physicalKey);
                    if (val instanceof RedisEntity cached) {
                        return "newApiKey".equals(cached.apiKey()); // 원하는 값으로 바뀌었는지 확인
                    }
                    return false;
                },
                500,
                50);

        assertThat(result.apiKey()).isEqualTo("newApiKey");

        Object actualValue = redisTemplate.opsForValue().get(physicalKey);
        assertThat(actualValue).isNotNull();
        assertThat(((RedisEntity) actualValue).apiKey()).isEqualTo("newApiKey");

        cleanCache(redisId);
    }

    @Test
    @DisplayName("deleteUser 확인 ")
    void deleteUser() throws InterruptedException {
        Integer redisId = generateUniqueId();
        String physicalKey = "redis::" + redisId;

        // Mock 설정
        RedisCustomEntity testEntity = createTestEntity(redisId, "nick", "apiKey");
        when(redisExampleCustomRepository.findById(redisId)).thenReturn(Optional.of(testEntity));

        // 캐시 생성
        redisService.getUser(redisId);

        // 캐시 생성 완료 대기 (최대 2초)
        waitForCondition(() -> Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey)), 2000, 50);

        assertThat(redisTemplate.hasKey(physicalKey)).isTrue();

        // 삭제 실행
        redisService.deleteUser(redisId);

        // DB 삭제 검증
        verify(redisExampleCustomRepository, times(1)).deleteById(redisId);

        // 캐시 삭제 완료 대기 (최대 2초)
        waitForCondition(() -> Boolean.FALSE.equals(redisTemplate.hasKey(physicalKey)), 2000, 50);

        assertThat(redisTemplate.hasKey(physicalKey)).isFalse();
    }

    @Test
    @DisplayName("자원공유 문제시 무조건 충돌나는 테스트")
    void forceFailureTest() throws InterruptedException {
        Integer redisId = generateUniqueId();
        String physicalKey = "redis::" + redisId;

        // 키가 없을 때까지 대기 (최대 500ms)
        waitForCondition(() -> Boolean.FALSE.equals(redisTemplate.hasKey(physicalKey)), 500, 50);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey))) {
            throw new RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.");
        }

        RedisCustomEntity testEntity = createTestEntity(redisId, "nick", "apiKey");

        // Redis에 데이터 저장
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        // 저장 완료 대기 (최대 500ms)
        waitForCondition(() -> Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey)), 500, 50);

        // 1초 대기 (의도적인 충돌 유발용)
        Thread.sleep(1000);

        // 삭제
        redisTemplate.delete(physicalKey);

        // 삭제 완료 대기 (최대 500ms)
        waitForCondition(() -> Boolean.FALSE.equals(redisTemplate.hasKey(physicalKey)), 500, 50);
    }

    // 헬퍼 메서드 추가
    private void waitForCondition(BooleanSupplier condition, long timeoutMs, long pollIntervalMs)
            throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new AssertionError("조건이 시간 내에 충족되지 않았습니다.");
            }
            Thread.sleep(pollIntervalMs);
        }
    }
}
