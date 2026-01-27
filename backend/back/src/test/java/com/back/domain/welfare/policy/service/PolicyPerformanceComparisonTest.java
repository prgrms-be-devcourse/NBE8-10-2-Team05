package com.back.domain.welfare.policy.service;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto;
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.domain.welfare.policy.search.PolicySearchCondition;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "logging.level.root=WARN",
            "logging.level.org.springframework=WARN",
            "logging.level.org.hibernate=WARN",
            "logging.level.org.hibernate.orm.jdbc=OFF",
            "logging.level.org.elasticsearch=WARN"
        })
@DisplayName("Policy 검색 성능 비교 테스트 (DB vs ElasticSearch)")
class PolicyPerformanceComparisonTest {

    private static final String INDEX = "policy";
    private static final int WARMUP_ITERATIONS = 3;
    private static final int TEST_ITERATIONS = 10;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyElasticSearchService policyElasticSearchService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private boolean elasticsearchAvailable = false;

    @BeforeEach
    @Transactional
    void setUp() throws IOException {
        // Elasticsearch 서버 연결 확인
        try {
            boolean pingResult = elasticsearchClient.ping().value();
            elasticsearchAvailable = pingResult;
            if (!elasticsearchAvailable) {
                System.out.println("⚠️ Elasticsearch 서버가 실행 중이지 않습니다. 모든 테스트를 건너뜁니다.");
                return;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Elasticsearch 서버 연결 실패: " + e.getMessage());
            System.out.println("⚠️ 모든 테스트를 건너뜁니다.");
            elasticsearchAvailable = false;
            return;
        }

        // 테스트 전 인덱스 삭제 (깨끗한 상태로 시작)
        try {
            if (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value()) {
                elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(INDEX)));
            }
        } catch (Exception e) {
            // 인덱스가 없으면 무시
        }

        // 테스트 전 DB 데이터 정리
        policyRepository.deleteAll();

        // 테스트 데이터 생성 (충분한 양의 데이터로 성능 테스트)
        // 주의: 데이터 양이 적으면(100개 이하) DB가 ES보다 빠를 수 있습니다.
        // ES의 장점은 수천~수만 건 이상의 대용량 데이터에서 발휘됩니다.
        // 더 많은 데이터로 테스트하려면: -Dtest.data.count=1000
        int testDataCount = Integer.parseInt(System.getProperty("test.data.count", "100"));
        createTestData(testDataCount);
        System.out.println("테스트 데이터 개수: " + testDataCount);

        // ES 인덱싱
        policyElasticSearchService.reindexAllFromDb();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (!elasticsearchAvailable) {
            return;
        }

        // 테스트 후 인덱스 정리
        try {
            if (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value()) {
                elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(INDEX)));
            }
        } catch (Exception e) {
            // 정리 실패는 무시
        }
    }

    @Test
    @DisplayName("나이 조건 검색 성능 비교")
    void comparePerformance_byAge() {
        skipIfCi();
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given: 나이 조건 검색
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(
                25, // sprtTrgtMinAge
                35, // sprtTrgtMaxAge
                null, null, null, null, null);

        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().age(30).build();

        // When & Then: 성능 측정 및 비교
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 결과 출력
        printComparisonResult("나이 조건 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("소득 조건 검색 성능 비교")
    void comparePerformance_byEarn() {
        skipIfCi();
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given: 소득 조건 검색
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(
                null,
                null,
                null,
                null,
                null,
                2000, // earnMinAmt
                5000 // earnMaxAmt
                );

        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().earn(3000).build();

        // When & Then: 성능 측정 및 비교
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 결과 출력
        printComparisonResult("소득 조건 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("지역 코드 검색 성능 비교")
    void comparePerformance_byRegion() {
        skipIfCi();
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given: 지역 코드 검색
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(
                null, null, "11", // zipCd
                null, null, null, null);

        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().regionCode("11").build();

        // When & Then: 성능 측정 및 비교
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 결과 출력
        printComparisonResult("지역 코드 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("복합 조건 검색 성능 비교")
    void comparePerformance_byMultipleConditions() {
        skipIfCi();
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given: 복합 조건 검색
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(
                25, // sprtTrgtMinAge
                35, // sprtTrgtMaxAge
                "11", // zipCd
                "S01", // schoolCd
                "J01", // jobCd
                2000, // earnMinAmt
                5000 // earnMaxAmt
                );

        PolicySearchCondition esCondition = PolicySearchCondition.builder()
                .age(30)
                .earn(3000)
                .regionCode("11")
                .schoolCode("S01")
                .jobCode("J01")
                .build();

        // When & Then: 성능 측정 및 비교
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 결과 출력
        printComparisonResult("복합 조건 검색", dbResult, esResult);
    }

    @Test
    @DisplayName("키워드 검색 성능 비교 (ES만 지원)")
    void comparePerformance_byKeyword() {
        skipIfCi();
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given: 키워드 검색 (ES만 지원)
        PolicySearchCondition esCondition =
                PolicySearchCondition.builder().keyword("청년").build();

        // When & Then: ES 성능만 측정
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 결과 출력
        System.out.println("\n=== 키워드 검색 성능 (ES만 지원) ===");
        System.out.printf("ES 평균 시간: %.2f ms\n", esResult.averageTimeMs);
        System.out.printf("ES 최소 시간: %.2f ms\n", (double) esResult.minTimeMs);
        System.out.printf("ES 최대 시간: %.2f ms\n", (double) esResult.maxTimeMs);
        System.out.printf("ES 결과 개수: %d\n", esResult.resultCount);
    }

    @Test
    @DisplayName("전체 검색 성능 비교 (조건 없음)")
    void comparePerformance_noCondition() {
        skipIfCi();
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        // Given: 조건 없는 전체 검색
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(null, null, null, null, null, null, null);

        PolicySearchCondition esCondition = PolicySearchCondition.builder().build();

        // When & Then: 성능 측정 및 비교
        PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
        PerformanceResult esResult = measureEsPerformance(() -> {
            try {
                return policyElasticSearchService.search(esCondition, 0, 100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 결과 출력
        printComparisonResult("전체 검색 (조건 없음)", dbResult, esResult);
    }

    @Test
    @DisplayName("데이터 양에 따른 성능 비교 (100 vs 1000 vs 10000)")
    void comparePerformance_byDataSize() throws IOException {
        skipIfCi();
        assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

        int[] dataSizes = {100, 1000, 10000};
        PolicySearchRequestDto dbRequest = new PolicySearchRequestDto(25, 35, "11", "S01", "J01", 2000, 5000);
        PolicySearchCondition esCondition = PolicySearchCondition.builder()
                .age(30)
                .earn(3000)
                .regionCode("11")
                .schoolCode("S01")
                .jobCode("J01")
                .build();

        System.out.println("\n=== 데이터 양에 따른 성능 비교 ===");
        for (int size : dataSizes) {
            // 데이터 재생성
            policyRepository.deleteAll();
            createTestData(size);
            policyElasticSearchService.reindexAllFromDb();

            System.out.println("\n--- 데이터 개수: " + size + " ---");

            PerformanceResult dbResult = measureDbPerformance(() -> policyService.search(dbRequest));
            PerformanceResult esResult = measureEsPerformance(() -> {
                try {
                    return policyElasticSearchService.search(esCondition, 0, 100);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            double speedup = dbResult.averageTimeMs / esResult.averageTimeMs;
            System.out.printf("DB 평균: %.2f ms, ES 평균: %.2f ms", dbResult.averageTimeMs, esResult.averageTimeMs);
            if (speedup > 1.0) {
                System.out.printf(" → ES가 %.2f배 빠름\n", speedup);
            } else {
                System.out.printf(" → DB가 %.2f배 빠름\n", 1.0 / speedup);
            }
        }
    }

    /* ===== CI 환경 체크 유틸리티 ===== */

    private void skipIfCi() {
        String ciEnv = System.getenv("CI");
        String githubActions = System.getenv("GITHUB_ACTIONS");
        boolean isCi = ciEnv != null && !ciEnv.isEmpty() || githubActions != null && !githubActions.isEmpty();
        assumeTrue(!isCi, "CI 환경에서는 성능 테스트를 실행하지 않습니다. 로컬 환경에서만 실행하세요.");
    }

    /* ===== 성능 측정 유틸리티 ===== */

    private PerformanceResult measureDbPerformance(
            java.util.function.Supplier<List<PolicySearchResponseDto>> searchFunction) {
        // 워밍업
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            searchFunction.get();
        }

        // 실제 측정
        List<Long> times = new ArrayList<>();
        int resultCount = 0;

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            List<PolicySearchResponseDto> results = searchFunction.get();
            long endTime = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            times.add(duration);
            if (i == 0) {
                resultCount = results.size();
            }
        }

        return calculatePerformanceResult(times, resultCount);
    }

    private PerformanceResult measureEsPerformance(java.util.function.Supplier<List<?>> searchFunction) {
        // 워밍업
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            searchFunction.get();
        }

        // 실제 측정
        List<Long> times = new ArrayList<>();
        int resultCount = 0;

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            List<?> results = searchFunction.get();
            long endTime = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            times.add(duration);
            if (i == 0) {
                resultCount = results.size();
            }
        }

        return calculatePerformanceResult(times, resultCount);
    }

    private PerformanceResult calculatePerformanceResult(List<Long> times, int resultCount) {
        double average = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = times.stream().mapToLong(Long::longValue).max().orElse(0);

        return new PerformanceResult(average, min, max, resultCount);
    }

    private void printComparisonResult(String testName, PerformanceResult dbResult, PerformanceResult esResult) {
        System.out.println("\n=== " + testName + " 성능 비교 ===");
        System.out.printf("DB 평균 시간: %.2f ms\n", dbResult.averageTimeMs);
        System.out.printf("DB 최소 시간: %.2f ms\n", (double) dbResult.minTimeMs);
        System.out.printf("DB 최대 시간: %.2f ms\n", (double) dbResult.maxTimeMs);
        System.out.printf("DB 결과 개수: %d\n", dbResult.resultCount);

        System.out.printf("\nES 평균 시간: %.2f ms\n", esResult.averageTimeMs);
        System.out.printf("ES 최소 시간: %.2f ms\n", (double) esResult.minTimeMs);
        System.out.printf("ES 최대 시간: %.2f ms\n", (double) esResult.maxTimeMs);
        System.out.printf("ES 결과 개수: %d\n", esResult.resultCount);

        double speedup = dbResult.averageTimeMs / esResult.averageTimeMs;
        if (speedup > 1.0) {
            System.out.printf("\n✅ ES가 DB보다 %.2f배 빠릅니다.\n", speedup);
        } else {
            System.out.printf("\n✅ DB가 ES보다 %.2f배 빠릅니다.\n", 1.0 / speedup);
        }
        System.out.println();
    }

    /* ===== 테스트 데이터 생성 ===== */

    private void createTestData(int count) {
        List<Policy> policies = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            int ageGroup = i % 4; // 0-3 그룹으로 나눔
            int earnGroup = i % 5; // 0-4 그룹으로 나눔

            Policy policy = Policy.builder()
                    .plcyNo("PERF-TEST-" + i + "-" + uniqueId)
                    .plcyNm("성능 테스트 정책 " + i)
                    .sprtTrgtMinAge(String.valueOf(20 + ageGroup * 10))
                    .sprtTrgtMaxAge(String.valueOf(29 + ageGroup * 10))
                    .sprtTrgtAgeLmtYn("Y")
                    .earnCndSeCd("연소득")
                    .earnMinAmt(String.valueOf(earnGroup * 1000))
                    .earnMaxAmt(String.valueOf((earnGroup + 1) * 1000))
                    .zipCd(i % 2 == 0 ? "11" : "26")
                    .jobCd(i % 3 == 0 ? "J01" : (i % 3 == 1 ? "J02" : "J03"))
                    .schoolCd(i % 2 == 0 ? "S01" : "S02")
                    .mrgSttsCd(i % 2 == 0 ? "N" : "Y")
                    .plcyKywdNm("테스트,성능,정책")
                    .plcyExplnCn("성능 테스트를 위한 정책 설명 " + i)
                    .build();

            policies.add(policy);
        }

        policyRepository.saveAll(policies);
    }

    /* ===== 성능 결과 클래스 ===== */

    private static class PerformanceResult {
        final double averageTimeMs;
        final long minTimeMs;
        final long maxTimeMs;
        final int resultCount;

        PerformanceResult(double averageTimeMs, long minTimeMs, long maxTimeMs, int resultCount) {
            this.averageTimeMs = averageTimeMs;
            this.minTimeMs = minTimeMs;
            this.maxTimeMs = maxTimeMs;
            this.resultCount = resultCount;
        }
    }
}
