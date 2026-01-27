package com.back.domain.welfare.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.document.PolicyDocument;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.domain.welfare.policy.search.PolicySearchCondition;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PolicyElasticSearchService 통합 테스트")
class PolicyElasticSearchServiceIntegrationTest {

    private static final String INDEX = "policy";
    // Elasticsearch 동기화 대기 설정
    private static final int MAX_WAIT_ATTEMPTS = 30; // 최대 30번 시도
    private static final long WAIT_INTERVAL_MS = 200; // 200ms 간격

    @Autowired
    private PolicyElasticSearchService policyElasticSearchService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyDocumentMapper policyDocumentMapper;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private boolean elasticsearchAvailable = false;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        // Elasticsearch 서버 연결 확인
        try {
            boolean pingResult = elasticsearchClient.ping().value();
            elasticsearchAvailable = pingResult;
            if (!elasticsearchAvailable) {
                System.out.println("⚠️ Elasticsearch 서버가 실행 중이지 않습니다. 테스트를 건너뜁니다.");
                return;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Elasticsearch 서버 연결 실패: " + e.getMessage());
            System.out.println("⚠️ 테스트를 건너뜁니다.");
            elasticsearchAvailable = false;
            return;
        }

        // 테스트 전 인덱스 삭제 (깨끗한 상태로 시작)
        try {
            if (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value()) {
                elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(INDEX)));
                // 인덱스 삭제 완료 대기
                waitForIndexDeletion();
            }
        } catch (Exception e) {
            // 인덱스가 없으면 무시
        }

        // 테스트 전 DB 데이터 정리
        policyRepository.deleteAll();
        policyRepository.flush();
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        if (!elasticsearchAvailable) {
            return;
        }

        // 테스트 후 인덱스 정리
        try {
            if (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value()) {
                elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(INDEX)));
                waitForIndexDeletion();
            }
        } catch (Exception e) {
            // 정리 실패는 무시
        }
    }

    /**
     * 인덱스 삭제가 완료될 때까지 대기
     */
    private void waitForIndexDeletion() throws IOException, InterruptedException {
        for (int i = 0; i < MAX_WAIT_ATTEMPTS; i++) {
            try {
                boolean exists = elasticsearchClient
                        .indices()
                        .exists(e -> e.index(INDEX))
                        .value();
                if (!exists) {
                    return; // 삭제 완료
                }
            } catch (Exception e) {
                // 인덱스가 없으면 성공
                return;
            }
            Thread.sleep(WAIT_INTERVAL_MS);
        }
    }

    /**
     * Elasticsearch에 예상 문서 수가 인덱싱될 때까지 대기
     */
    private void waitForDocumentCount(long expectedCount) throws IOException, InterruptedException {
        elasticsearchClient.indices().refresh(r -> r.index(INDEX));

        for (int i = 0; i < MAX_WAIT_ATTEMPTS; i++) {
            try {
                long actualCount = elasticsearchClient
                        .count(CountRequest.of(c -> c.index(INDEX)))
                        .count();

                if (actualCount >= expectedCount) {
                    System.out.println("✅ 문서 인덱싱 완료: " + actualCount + " / " + expectedCount);
                    // 추가 안정화 대기
                    Thread.sleep(500);
                    return;
                }

                System.out.println("⏳ 대기 중... (" + actualCount + " / " + expectedCount + ")");
            } catch (Exception e) {
                // 인덱스가 아직 준비되지 않았을 수 있음
            }
            Thread.sleep(WAIT_INTERVAL_MS);
        }

        // 타임아웃
        System.err.println("⚠️ 타임아웃: 예상 문서 수 " + expectedCount + "개가 인덱싱되지 않았습니다.");
    }

    /**
     * Elasticsearch 인덱스를 refresh하고 문서가 검색 가능할 때까지 대기
     */
    private void refreshAndWaitForSearch(long expectedMinCount) throws IOException, InterruptedException {
        elasticsearchClient.indices().refresh(r -> r.index(INDEX));

        for (int i = 0; i < MAX_WAIT_ATTEMPTS; i++) {
            try {
                var searchResponse = elasticsearchClient.search(
                        s -> s.index(INDEX).query(q -> q.matchAll(m -> m)), PolicyDocument.class);

                long actualCount = searchResponse.hits().total().value();
                if (actualCount >= expectedMinCount) {
                    System.out.println("✅ 검색 가능 확인: " + actualCount + " 건");
                    Thread.sleep(300); // 추가 안정화
                    return;
                }
            } catch (Exception e) {
                // 아직 준비되지 않음
            }
            Thread.sleep(WAIT_INTERVAL_MS);
        }

        System.err.println("⚠️ 검색 타임아웃");
    }

    @Nested
    @DisplayName("인덱스 관리")
    class IndexManagement {

        @Test
        @DisplayName("ensureIndex: 인덱스가 없으면 생성")
        void ensureIndex_createsIndexWhenNotExists() throws IOException, InterruptedException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given: 인덱스가 없는 상태

            // When
            policyElasticSearchService.ensureIndex();

            // 인덱스 생성 대기
            for (int i = 0; i < MAX_WAIT_ATTEMPTS; i++) {
                try {
                    boolean exists = elasticsearchClient
                            .indices()
                            .exists(e -> e.index(INDEX))
                            .value();
                    if (exists) {
                        break;
                    }
                } catch (Exception e) {
                    // 계속 시도
                }
                Thread.sleep(WAIT_INTERVAL_MS);
            }

            // Then: 인덱스가 생성되었는지 확인
            boolean exists =
                    elasticsearchClient.indices().exists(e -> e.index(INDEX)).value();
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("ensureIndex: 인덱스가 이미 있으면 재생성하지 않음")
        void ensureIndex_doesNotRecreateWhenExists() throws IOException, InterruptedException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given: 인덱스 생성
            policyElasticSearchService.ensureIndex();

            // 인덱스 생성 대기
            for (int i = 0; i < MAX_WAIT_ATTEMPTS; i++) {
                try {
                    boolean exists = elasticsearchClient
                            .indices()
                            .exists(e -> e.index(INDEX))
                            .value();
                    if (exists) {
                        break;
                    }
                } catch (Exception e) {
                    // 계속 시도
                }
                Thread.sleep(WAIT_INTERVAL_MS);
            }

            boolean firstExists =
                    elasticsearchClient.indices().exists(e -> e.index(INDEX)).value();
            assertThat(firstExists).isTrue();

            // When: 다시 ensureIndex 호출
            policyElasticSearchService.ensureIndex();
            Thread.sleep(500);

            // Then: 인덱스가 여전히 존재
            boolean stillExists =
                    elasticsearchClient.indices().exists(e -> e.index(INDEX)).value();
            assertThat(stillExists).isTrue();
        }
    }

    @Nested
    @DisplayName("문서 인덱싱")
    class DocumentIndexing {

        @Test
        @Transactional
        @DisplayName("reindexAllFromDb: DB의 Policy를 ES에 인덱싱")
        void reindexAllFromDb_indexesAllPolicies() throws IOException, InterruptedException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given: DB에 Policy 데이터 생성
            String uniqueId1 = UUID.randomUUID().toString().substring(0, 8);
            String uniqueId2 = UUID.randomUUID().toString().substring(0, 8);
            Policy policy1 = Policy.builder()
                    .plcyNo("TEST-001-" + uniqueId1)
                    .plcyNm("청년 주거 지원 정책")
                    .sprtTrgtMinAge("20")
                    .sprtTrgtMaxAge("39")
                    .sprtTrgtAgeLmtYn("Y")
                    .earnCndSeCd("연소득")
                    .earnMinAmt("0")
                    .earnMaxAmt("5000")
                    .zipCd("11")
                    .jobCd("J01")
                    .schoolCd("S01")
                    .mrgSttsCd("N")
                    .plcyKywdNm("청년,주거,취업")
                    .plcyExplnCn("청년을 위한 주거 지원 정책입니다")
                    .build();

            Policy policy2 = Policy.builder()
                    .plcyNo("TEST-002-" + uniqueId2)
                    .plcyNm("중장년 취업 지원")
                    .sprtTrgtMinAge("40")
                    .sprtTrgtMaxAge("65")
                    .sprtTrgtAgeLmtYn("Y")
                    .earnCndSeCd("무관")
                    .zipCd("11")
                    .jobCd("J02")
                    .plcyKywdNm("취업,중장년")
                    .plcyExplnCn("중장년층 취업을 지원하는 정책입니다")
                    .build();

            policyRepository.save(policy1);
            policyRepository.save(policy2);
            policyRepository.flush();

            // When: reindexAllFromDb 실행
            long indexedCount = policyElasticSearchService.reindexAllFromDb();

            // CRITICAL: 문서가 검색 가능해질 때까지 대기
            waitForDocumentCount(2);

            // Then: 인덱싱된 문서 수 확인
            assertThat(indexedCount).isGreaterThanOrEqualTo(2);

            // ES에서 문서 조회 확인
            var searchResponse = elasticsearchClient.search(
                    s -> s.index(INDEX).query(q -> q.matchAll(m -> m)), PolicyDocument.class);

            assertThat(searchResponse.hits().total().value()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @Transactional
        @DisplayName("reindexAllFromDb: DB에 데이터가 없으면 0 반환")
        void reindexAllFromDb_returnsZeroWhenNoData() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given: DB에 Policy 데이터가 없는 상태
            policyRepository.deleteAll();
            policyRepository.flush();

            // When
            long indexedCount = policyElasticSearchService.reindexAllFromDb();

            // Then
            assertThat(indexedCount).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("키워드 검색")
    class KeywordSearch {

        @BeforeEach
        @Transactional
        void setUp() throws IOException, InterruptedException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // 기존 데이터 정리
            policyRepository.deleteAll();
            policyRepository.flush();

            // 테스트 데이터 준비
            String uniqueId1 = UUID.randomUUID().toString().substring(0, 8);
            String uniqueId2 = UUID.randomUUID().toString().substring(0, 8);
            String uniqueId3 = UUID.randomUUID().toString().substring(0, 8);

            Policy policy1 = Policy.builder()
                    .plcyNo("SEARCH-001-" + uniqueId1)
                    .plcyNm("청년 주거 지원")
                    .sprtTrgtMinAge("20")
                    .sprtTrgtMaxAge("39")
                    .sprtTrgtAgeLmtYn("Y")
                    .earnCndSeCd("연소득")
                    .earnMinAmt("0")
                    .earnMaxAmt("5000")
                    .zipCd("11")
                    .jobCd("J01")
                    .schoolCd("S01")
                    .mrgSttsCd("N")
                    .plcyKywdNm("청년,주거")
                    .plcyExplnCn("청년을 위한 주거 지원 정책")
                    .build();

            Policy policy2 = Policy.builder()
                    .plcyNo("SEARCH-002-" + uniqueId2)
                    .plcyNm("중장년 취업 지원")
                    .sprtTrgtMinAge("40")
                    .sprtTrgtMaxAge("65")
                    .sprtTrgtAgeLmtYn("Y")
                    .earnCndSeCd("무관")
                    .zipCd("26")
                    .jobCd("J02")
                    .plcyKywdNm("취업,중장년")
                    .plcyExplnCn("중장년 취업을 지원합니다")
                    .build();

            Policy policy3 = Policy.builder()
                    .plcyNo("SEARCH-003-" + uniqueId3)
                    .plcyNm("전체 교육 지원")
                    .sprtTrgtMinAge("18")
                    .sprtTrgtMaxAge("70")
                    .sprtTrgtAgeLmtYn("Y")
                    .earnCndSeCd("무관")
                    .zipCd("11")
                    .schoolCd("S02")
                    .plcyKywdNm("교육")
                    .plcyExplnCn("모든 연령 교육 지원")
                    .build();

            policyRepository.save(policy1);
            policyRepository.save(policy2);
            policyRepository.save(policy3);
            policyRepository.flush();

            // Elasticsearch 재인덱싱 및 검색 가능 대기
            policyElasticSearchService.reindexAllFromDb();
            waitForDocumentCount(3);
        }

        @Test
        @DisplayName("search: 키워드로 검색")
        void search_byKeyword() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("청년").build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
            results.forEach(doc -> {
                String content = (doc.getPlcyNm() + " " + doc.getKeywords()).toLowerCase();
                assertThat(content).contains("청년");
            });
        }

        @Test
        @DisplayName("search: 나이로 필터링")
        void search_byAge() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().age(25).build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
            results.forEach(doc -> {
                if (doc.getMinAge() != null && doc.getMaxAge() != null) {
                    assertThat(doc.getMinAge()).isLessThanOrEqualTo(25);
                    assertThat(doc.getMaxAge()).isGreaterThanOrEqualTo(25);
                }
            });
        }

        @Test
        @DisplayName("search: 소득 조건으로 필터링")
        void search_byEarn() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().earn(3000).build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
            results.forEach(doc -> {
                if (doc.getEarnMin() != null && doc.getEarnMax() != null) {
                    assertThat(doc.getEarnMin()).isLessThanOrEqualTo(3000);
                    assertThat(doc.getEarnMax()).isGreaterThanOrEqualTo(3000);
                }
            });
        }

        @Test
        @DisplayName("search: 지역 코드로 필터링")
        void search_byRegionCode() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().regionCode("11").build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
            results.forEach(doc -> {
                if (doc.getRegionCode() != null) {
                    assertThat(doc.getRegionCode()).isEqualTo("11");
                }
            });
        }

        @Test
        @DisplayName("search: 직업 코드로 필터링")
        void search_byJobCode() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().jobCode("J01").build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
            results.forEach(doc -> {
                if (doc.getJobCode() != null) {
                    assertThat(doc.getJobCode()).isEqualTo("J01");
                }
            });
        }

        @Test
        @DisplayName("search: 결혼 상태로 필터링")
        void search_byMarriageStatus() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().marriageStatus("N").build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
            results.forEach(doc -> {
                if (doc.getMarriageStatus() != null) {
                    assertThat(doc.getMarriageStatus()).isEqualTo("N");
                }
            });
        }

        @Test
        @DisplayName("search: 키워드 태그로 필터링")
        void search_byKeywords() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition = PolicySearchCondition.builder()
                    .keywords(List.of("청년", "주거"))
                    .build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("search: 복합 조건 검색")
        void search_byMultipleConditions() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition = PolicySearchCondition.builder()
                    .keyword("청년")
                    .age(25)
                    .earn(3000)
                    .regionCode("11")
                    .jobCode("J01")
                    .marriageStatus("N")
                    .keywords(List.of("주거"))
                    .build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("search: 조건이 없으면 전체 검색")
        void search_returnsAllWhenNoCondition() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition = PolicySearchCondition.builder().build();

            // When
            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            // Then
            assertThat(results).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("검색 결과 총 개수 포함")
    class SearchWithTotal {

        @BeforeEach
        @Transactional
        void setUp() throws IOException, InterruptedException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // 기존 데이터 정리
            policyRepository.deleteAll();
            policyRepository.flush();

            // 테스트 데이터 준비
            for (int i = 1; i <= 5; i++) {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                Policy policy = Policy.builder()
                        .plcyNo("TOTAL-" + i + "-" + uniqueId)
                        .plcyNm("테스트 정책 " + i)
                        .plcyKywdNm("테스트")
                        .plcyExplnCn("테스트 정책 설명 " + i)
                        .build();
                policyRepository.save(policy);
            }
            policyRepository.flush();

            policyElasticSearchService.reindexAllFromDb();

            // CRITICAL: 5개 문서가 모두 검색 가능해질 때까지 대기
            waitForDocumentCount(5);
        }

        @Test
        @DisplayName("searchWithTotal: 문서 목록과 총 개수 반환")
        void searchWithTotal_returnsDocumentsAndTotal() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("테스트").build();

            // When
            PolicyElasticSearchService.SearchResult result =
                    policyElasticSearchService.searchWithTotal(condition, 0, 10);

            // Then
            assertThat(result.getDocuments()).isNotEmpty();
            assertThat(result.getTotal()).isGreaterThanOrEqualTo(5);
            assertThat(result.getTotal())
                    .isGreaterThanOrEqualTo(result.getDocuments().size());
        }

        @Test
        @DisplayName("searchWithTotal: 페이지네이션 시 총 개수는 전체 개수")
        void searchWithTotal_totalIsFullCount() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("테스트").build();

            // When
            PolicyElasticSearchService.SearchResult page1 = policyElasticSearchService.searchWithTotal(condition, 0, 2);
            PolicyElasticSearchService.SearchResult page2 = policyElasticSearchService.searchWithTotal(condition, 2, 2);

            // Then
            assertThat(page1.getTotal()).isEqualTo(page2.getTotal());
            assertThat(page1.getTotal()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("searchWithTotal: 검색 결과가 없으면 total은 0")
        void searchWithTotal_returnsZeroWhenNoResults() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            // Given
            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("존재하지않는키워드12345").build();

            // When
            PolicyElasticSearchService.SearchResult result =
                    policyElasticSearchService.searchWithTotal(condition, 0, 10);

            // Then
            assertThat(result.getDocuments()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }
    }
}
