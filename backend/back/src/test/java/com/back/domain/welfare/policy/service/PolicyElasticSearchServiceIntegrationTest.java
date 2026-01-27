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
    private static final int MAX_WAIT_ATTEMPTS = 50;
    private static final long WAIT_INTERVAL_MS = 200;

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
        try {
            boolean pingResult = elasticsearchClient.ping().value();
            elasticsearchAvailable = pingResult;
            if (!elasticsearchAvailable) {
                System.out.println("⚠️ Elasticsearch 서버가 실행 중이지 않습니다. 테스트를 건너뜁니다.");
                return;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Elasticsearch 서버 연결 실패: " + e.getMessage());
            elasticsearchAvailable = false;
            return;
        }

        try {
            if (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value()) {
                elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(INDEX)));
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            // 인덱스가 없으면 무시
        }

        policyRepository.deleteAll();
        policyRepository.flush();
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        if (!elasticsearchAvailable) {
            return;
        }

        try {
            if (elasticsearchClient.indices().exists(e -> e.index(INDEX)).value()) {
                elasticsearchClient.indices().delete(DeleteIndexRequest.of(d -> d.index(INDEX)));
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // 정리 실패는 무시
        }
    }

    /**
     * Elasticsearch에 문서가 완전히 인덱싱되고 검색 가능해질 때까지 대기
     */
    private void waitForIndexing(long expectedCount) throws IOException, InterruptedException {
        // 1단계: refresh
        try {
            elasticsearchClient.indices().refresh(r -> r.index(INDEX));
        } catch (Exception e) {
            // 인덱스가 아직 없을 수 있음
        }

        // 2단계: count API로 실제 문서 수 확인
        for (int i = 0; i < MAX_WAIT_ATTEMPTS; i++) {
            try {
                long count = elasticsearchClient
                        .count(CountRequest.of(c -> c.index(INDEX)))
                        .count();

                if (count >= expectedCount) {
                    // 3단계: 실제 검색 테스트
                    var searchResponse = elasticsearchClient.search(
                            s -> s.index(INDEX).query(q -> q.matchAll(m -> m)), PolicyDocument.class);

                    if (searchResponse.hits().total().value() >= expectedCount) {
                        System.out.println("✅ 인덱싱 완료: " + count + "건");
                        Thread.sleep(500); // 최종 안정화
                        return;
                    }
                }

                if (i % 10 == 0) {
                    System.out.println("⏳ 대기 중... " + count + " / " + expectedCount);
                }
            } catch (Exception e) {
                // 인덱스가 준비 중일 수 있음
            }

            Thread.sleep(WAIT_INTERVAL_MS);
        }

        System.err.println("⚠️ 타임아웃: " + expectedCount + "건 인덱싱 대기 실패");
    }

    @Nested
    @DisplayName("인덱스 관리")
    class IndexManagement {

        @Test
        @DisplayName("ensureIndex: 인덱스가 없으면 생성")
        void ensureIndex_createsIndexWhenNotExists() throws IOException, InterruptedException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            policyElasticSearchService.ensureIndex();
            Thread.sleep(1000);

            boolean exists =
                    elasticsearchClient.indices().exists(e -> e.index(INDEX)).value();
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("ensureIndex: 인덱스가 이미 있으면 재생성하지 않음")
        void ensureIndex_doesNotRecreateWhenExists() throws IOException, InterruptedException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            policyElasticSearchService.ensureIndex();
            Thread.sleep(1000);

            boolean firstExists =
                    elasticsearchClient.indices().exists(e -> e.index(INDEX)).value();
            assertThat(firstExists).isTrue();

            policyElasticSearchService.ensureIndex();
            Thread.sleep(500);

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

            long indexedCount = policyElasticSearchService.reindexAllFromDb();
            waitForIndexing(2);

            assertThat(indexedCount).isGreaterThanOrEqualTo(2);

            var searchResponse = elasticsearchClient.search(
                    s -> s.index(INDEX).query(q -> q.matchAll(m -> m)), PolicyDocument.class);

            assertThat(searchResponse.hits().total().value()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @Transactional
        @DisplayName("reindexAllFromDb: DB에 데이터가 없으면 0 반환")
        void reindexAllFromDb_returnsZeroWhenNoData() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            policyRepository.deleteAll();
            policyRepository.flush();

            long indexedCount = policyElasticSearchService.reindexAllFromDb();

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

            policyRepository.deleteAll();
            policyRepository.flush();

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
                    .earnMinAmt("0")
                    .earnMaxAmt("3000")
                    .zipCd("11")
                    .jobCd("J01")
                    .schoolCd("S02")
                    .plcyKywdNm("교육")
                    .plcyExplnCn("모든 연령 교육 지원")
                    .build();

            policyRepository.save(policy1);
            policyRepository.save(policy2);
            policyRepository.save(policy3);
            policyRepository.flush();

            policyElasticSearchService.reindexAllFromDb();
            waitForIndexing(3);
        }

        @Test
        @DisplayName("search: 키워드 조건으로 검색")
        void search_byKeyword() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("청년").build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            assertThat(results).isNotEmpty();
            assertThat(results.stream().anyMatch(doc -> doc.getPlcyNm().contains("청년")))
                    .isTrue();
        }

        @Test
        @DisplayName("search: 나이 조건으로 필터링")
        void search_byAge() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().age(25).build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

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

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().earn(3000).build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

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

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().regionCode("11").build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

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

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().jobCode("J01").build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

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

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().marriageStatus("N").build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

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

            PolicySearchCondition condition = PolicySearchCondition.builder()
                    .keywords(List.of("청년", "주거"))
                    .build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("search: 복합 조건 검색")
        void search_byMultipleConditions() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            PolicySearchCondition condition = PolicySearchCondition.builder()
                    .keyword("청년")
                    .age(25)
                    .earn(3000)
                    .regionCode("11")
                    .jobCode("J01")
                    .marriageStatus("N")
                    .keywords(List.of("주거"))
                    .build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("search: 조건이 없으면 전체 검색")
        void search_returnsAllWhenNoCondition() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            PolicySearchCondition condition = PolicySearchCondition.builder().build();

            List<PolicyDocument> results = policyElasticSearchService.search(condition, 0, 10);

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

            policyRepository.deleteAll();
            policyRepository.flush();

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
            waitForIndexing(5);
        }

        @Test
        @DisplayName("searchWithTotal: 문서 목록과 총 개수 반환")
        void searchWithTotal_returnsDocumentsAndTotal() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("테스트").build();

            PolicyElasticSearchService.SearchResult result =
                    policyElasticSearchService.searchWithTotal(condition, 0, 10);

            assertThat(result.getDocuments()).isNotEmpty();
            assertThat(result.getTotal()).isGreaterThanOrEqualTo(5);
            assertThat(result.getTotal())
                    .isGreaterThanOrEqualTo(result.getDocuments().size());
        }

        @Test
        @DisplayName("searchWithTotal: 페이지네이션 시 총 개수는 전체 개수")
        void searchWithTotal_totalIsFullCount() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("테스트").build();

            PolicyElasticSearchService.SearchResult page1 = policyElasticSearchService.searchWithTotal(condition, 0, 2);
            PolicyElasticSearchService.SearchResult page2 = policyElasticSearchService.searchWithTotal(condition, 2, 2);

            assertThat(page1.getTotal()).isEqualTo(page2.getTotal());
            assertThat(page1.getTotal()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("searchWithTotal: 검색 결과가 없으면 total은 0")
        void searchWithTotal_returnsZeroWhenNoResults() throws IOException {
            assumeTrue(elasticsearchAvailable, "Elasticsearch 서버가 필요합니다");

            PolicySearchCondition condition =
                    PolicySearchCondition.builder().keyword("존재하지않는키워드12345").build();

            PolicyElasticSearchService.SearchResult result =
                    policyElasticSearchService.searchWithTotal(condition, 0, 10);

            assertThat(result.getDocuments()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }
    }
}
