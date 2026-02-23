package com.back.global.redis;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Execution(ExecutionMode.CONCURRENT) // ✅ 멀티코어 병렬 실행
class RedisServiceParallelTest {

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private RedisExampleCustomRepository redisExampleCustomRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String redisName = "redis";

    // 충돌 카운터
    private static final AtomicInteger conflictCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        Mockito.reset(redisExampleCustomRepository);
    }

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

    // ============================================
    // 멀티코어 충돌 테스트 (고정 키 사용)
    // ============================================

    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #1")
    void multiCoreConflictTest1() throws InterruptedException {
        // ❌ 고정 키 사용 → 다른 테스트와 충돌 발생!
        String physicalKey = "redis::SHARED_KEY";

        if (Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey))) {
            conflictCount.incrementAndGet();
            System.out.println("❌ [Test #1] 충돌 발생! 다른 테스트가 이미 키를 사용 중입니다.");
            throw new RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.");
        }

        RedisCustomEntity testEntity = createTestEntity(1, "nick", "apiKey");
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        System.out.println("✅ [Test #1] 키 생성 완료, 1초 대기 시작...");
        Thread.sleep(1000); // 다른 테스트가 접근할 시간 제공

        redisTemplate.delete(physicalKey);
        System.out.println("✅ [Test #1] 키 삭제 완료");
    }

    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #2")
    void multiCoreConflictTest2() throws InterruptedException {
        String physicalKey = "redis::SHARED_KEY";

        if (Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey))) {
            conflictCount.incrementAndGet();
            System.out.println("❌ [Test #2] 충돌 발생! 다른 테스트가 이미 키를 사용 중입니다.");
            throw new RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.");
        }

        RedisCustomEntity testEntity = createTestEntity(2, "nick2", "apiKey2");
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        System.out.println("✅ [Test #2] 키 생성 완료, 1초 대기 시작...");
        Thread.sleep(1000);

        redisTemplate.delete(physicalKey);
        System.out.println("✅ [Test #2] 키 삭제 완료");
    }

    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #3")
    void multiCoreConflictTest3() throws InterruptedException {
        String physicalKey = "redis::SHARED_KEY";

        if (Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey))) {
            conflictCount.incrementAndGet();
            System.out.println("❌ [Test #3] 충돌 발생! 다른 테스트가 이미 키를 사용 중입니다.");
            throw new RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.");
        }

        RedisCustomEntity testEntity = createTestEntity(3, "nick3", "apiKey3");
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        System.out.println("✅ [Test #3] 키 생성 완료, 1초 대기 시작...");
        Thread.sleep(1000);

        redisTemplate.delete(physicalKey);
        System.out.println("✅ [Test #3] 키 삭제 완료");
    }

    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #4")
    void multiCoreConflictTest4() throws InterruptedException {
        String physicalKey = "redis::SHARED_KEY";

        if (Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey))) {
            conflictCount.incrementAndGet();
            System.out.println("❌ [Test #4] 충돌 발생! 다른 테스트가 이미 키를 사용 중입니다.");
            throw new RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.");
        }

        RedisCustomEntity testEntity = createTestEntity(4, "nick4", "apiKey4");
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        System.out.println("✅ [Test #4] 키 생성 완료, 1초 대기 시작...");
        Thread.sleep(1000);

        redisTemplate.delete(physicalKey);
        System.out.println("✅ [Test #4] 키 삭제 완료");
    }

    @Test
    @DisplayName("멀티코어 자원공유 문제 - 충돌 감지 테스트 #5")
    void multiCoreConflictTest5() throws InterruptedException {
        String physicalKey = "redis::SHARED_KEY";

        if (Boolean.TRUE.equals(redisTemplate.hasKey(physicalKey))) {
            conflictCount.incrementAndGet();
            System.out.println("❌ [Test #5] 충돌 발생! 다른 테스트가 이미 키를 사용 중입니다.");
            throw new RuntimeException("병렬 경합 발생! 옆 프로세스가 이미 자원을 쓰고 있습니다.");
        }

        RedisCustomEntity testEntity = createTestEntity(5, "nick5", "apiKey5");
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        System.out.println("✅ [Test #5] 키 생성 완료, 1초 대기 시작...");
        Thread.sleep(1000);

        redisTemplate.delete(physicalKey);
        System.out.println("✅ [Test #5] 키 삭제 완료");
    }

    // ============================================
    // 대조군: Unique ID 사용 (안전한 테스트)
    // ============================================

    @Test
    @DisplayName("멀티코어 안전 테스트 - Unique ID #1")
    void multiCoreSafeTest1() throws InterruptedException {
        Integer redisId = generateUniqueId(); // ✅ 유니크 키
        String physicalKey = "redis::" + redisId;

        RedisCustomEntity testEntity = createTestEntity(redisId, "safe1", "key1");
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        System.out.println("✅ [Safe #1] 유니크 키 사용, 충돌 없음");
        Thread.sleep(500);

        redisTemplate.delete(physicalKey);
    }

    @Test
    @DisplayName("멀티코어 안전 테스트 - Unique ID #2")
    void multiCoreSafeTest2() throws InterruptedException {
        Integer redisId = generateUniqueId();
        String physicalKey = "redis::" + redisId;

        RedisCustomEntity testEntity = createTestEntity(redisId, "safe2", "key2");
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        System.out.println("✅ [Safe #2] 유니크 키 사용, 충돌 없음");
        Thread.sleep(500);

        redisTemplate.delete(physicalKey);
    }

    @Test
    @DisplayName("멀티코어 안전 테스트 - Unique ID #3")
    void multiCoreSafeTest3() throws InterruptedException {
        Integer redisId = generateUniqueId();
        String physicalKey = "redis::" + redisId;

        RedisCustomEntity testEntity = createTestEntity(redisId, "safe3", "key3");
        redisTemplate.opsForValue().set(physicalKey, RedisEntity.from(testEntity));

        System.out.println("✅ [Safe #3] 유니크 키 사용, 충돌 없음");
        Thread.sleep(500);

        redisTemplate.delete(physicalKey);
    }

    // ============================================
    // 충돌 통계 확인
    // ============================================

    @AfterAll
    static void printConflictStats() {
        System.out.println("\n========================================");
        System.out.println("멀티코어 테스트 결과");
        System.out.println("========================================");
        System.out.println("총 충돌 발생 횟수: " + conflictCount.get());
        System.out.println("예상: 고정 키 사용 시 여러 테스트가 충돌");
        System.out.println("실제: Unique ID 사용 시 충돌 없음");
        System.out.println("========================================\n");
    }
}
