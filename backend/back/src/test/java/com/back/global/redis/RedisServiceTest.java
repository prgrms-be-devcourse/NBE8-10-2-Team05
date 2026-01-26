package com.back.global.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
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
class RedisServiceTest {
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
    @DisplayName("Redis DB에 물리적으로 데이터가 저장되었는지 확인")
    void checkPhysicalRedis() {
        Integer redisId = 100;
        testEntity = RedisCustomEntity.builder().id(100).nickname("redis").build();
        when(redisExampleCustomRepository.findById(redisId)).thenReturn(Optional.of(testEntity));

        // 스프링 캐시가 생성하는 규칙에 따른 실제 키값 생성 (value::key)
        String expectedKey = "redis::" + redisId;

        redisTemplate.opsForValue().set(expectedKey, RedisEntity.from(testEntity));

        Object actualValue = redisTemplate.opsForValue().get(expectedKey);

        assertThat(actualValue).isNotNull();
        assertThat(actualValue).isInstanceOf(RedisEntity.class); // 직렬화가 잘 되어 객체로 복원되는지 확인

        RedisEntity cachedEntity = (RedisEntity) actualValue;
        assertThat(cachedEntity.id()).isEqualTo(100);
        assertThat(cachedEntity.nickname()).isEqualTo("redis");
    }

    @Test
    @DisplayName("getUser 확인")
    void getUser() {
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
    @DisplayName("updateUser 확인")
    void updateUser() {
        Integer redisId = 1;
        when(redisExampleCustomRepository.findById(redisId)).thenReturn(Optional.of(testEntity));

        RedisEntity result = redisService.updateUser(redisId, "newApiKey");

        String expectedKey = "redis::" + redisId;
        Object actualValue = redisTemplate.opsForValue().get(expectedKey);

        assertThat(result.id()).isEqualTo(testEntity.id());
        assertThat(result.apiKey()).isEqualTo("newApiKey");

        assertThat(actualValue).isNotNull();
        RedisEntity cachedEntity = (RedisEntity) actualValue;
        assertThat(cachedEntity.id()).isEqualTo(1);
        assertThat(cachedEntity.apiKey()).isEqualTo("newApiKey");
    }

    @Test
    @DisplayName("deleteUser 확인 ")
    void deleteUser() {
        Integer redisId = 1;
        // Mock이므로 실제 데이터 유무와 상관없이 호출은 성공함
        when(redisExampleCustomRepository.findById(redisId)).thenReturn(Optional.of(testEntity));
        doNothing().when(redisExampleCustomRepository).deleteById(redisId);

        // service호출 대신 실제 cache사용
        // service에 @Tramsactional 붙어있으면 테스트가 어렵기 때문
        //        Cache cache = cacheManager.getCache("redis");
        //        cache.put(redisId, RedisEntity.from(testEntity));
        redisService.getUser(redisId);

        assertThat(cacheManager.getCache("redis").get(redisId)).isNotNull();

        redisService.deleteUser(redisId);

        assertThat(cacheManager.getCache("redis").get(redisId)).isNull();
    }
}
