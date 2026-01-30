package com.back.domain.welfare.policy.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.document.PolicyDocument;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.domain.welfare.policy.search.PolicyQueryBuilder;
import com.back.domain.welfare.policy.search.PolicySearchCondition;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyElasticSearchService {
    private final ElasticsearchClient esClient;
    private final PolicyRepository policyRepository;
    private final PolicyDocumentMapper policyDocumentMapper;
    private final PolicyQueryBuilder policyQueryBuilder;

    private static final String INDEX = "policy";

    @Transactional
    public void deleteIndex() throws IOException {
        if (esClient.indices().exists(ExistsRequest.of(r -> r.index(INDEX))).value()) {
            esClient.indices().delete(d -> d.index(INDEX));
            log.info("Elasticsearch index deleted: {}", INDEX);
        }
    }

    @Transactional
    public long saveAll(List<Policy> policies) throws IOException {
        ensureIndex();

        if (policies.isEmpty()) {
            return 0;
        }

        List<BulkOperation> ops = new ArrayList<>(policies.size());
        for (Policy policy : policies) {
            PolicyDocument doc = policyDocumentMapper.toDocument(policy);
            if (doc == null || doc.getPolicyId() == null) {
                continue;
            }

            ops.add(BulkOperation.of(b -> b.index(
                    i -> i.index(INDEX).id(String.valueOf(doc.getPolicyId())).document(doc))));
        }

        var resp = esClient.bulk(b -> b.operations(ops).refresh(Refresh.True));
        if (resp.errors()) {
            log.warn("ES bulk save completed with errors.");
        } else {
            log.info("ES bulk save completed. items={}", ops.size());
        }

        return ops.size();
    }

    @Transactional
    public void ensureIndex() throws IOException {
        boolean exists =
                esClient.indices().exists(ExistsRequest.of(r -> r.index(INDEX))).value();

        if (exists) {
            return;
        }

        esClient.indices().create(c -> c.index(INDEX).mappings(m -> m.properties("policyId", p -> p.integer(i -> i))
                .properties("plcyNo", p -> p.keyword(k -> k))
                .properties("plcyNm", p -> p.text(t -> t))
                .properties("minAge", p -> p.integer(i -> i))
                .properties("maxAge", p -> p.integer(i -> i))
                .properties("ageLimited", p -> p.boolean_(b -> b))
                .properties("earnCondition", p -> p.keyword(k -> k))
                .properties("earnMin", p -> p.integer(i -> i))
                .properties("earnMax", p -> p.integer(i -> i))
                .properties("regionCode", p -> p.keyword(k -> k))
                .properties("jobCode", p -> p.keyword(k -> k))
                .properties("schoolCode", p -> p.keyword(k -> k))
                .properties("marriageStatus", p -> p.keyword(k -> k))
                .properties("keywords", p -> p.keyword(k -> k))
                .properties("specialBizCode", p -> p.keyword(k -> k))
                .properties("description", p -> p.text(t -> t))));

        log.info("Elasticsearch index created: {}", INDEX);
    }

    @Transactional
    public long reindexAllFromDb() throws IOException {
        ensureIndex();

        List<Policy> policies = policyRepository.findAll();
        if (policies.isEmpty()) {
            return 0;
        }

        List<BulkOperation> ops = new ArrayList<>(policies.size());
        for (Policy policy : policies) {
            PolicyDocument doc = policyDocumentMapper.toDocument(policy);
            if (doc == null || doc.getPolicyId() == null) {
                continue;
            }

            ops.add(BulkOperation.of(b -> b.index(
                    i -> i.index(INDEX).id(String.valueOf(doc.getPolicyId())).document(doc))));
        }

        var resp = esClient.bulk(b -> b.operations(ops).refresh(Refresh.True));
        if (resp.errors()) {
            log.warn(
                    "Elasticsearch bulk reindex completed with errors. took={}, items={}",
                    resp.took(),
                    resp.items().size());
        } else {
            log.info(
                    "Elasticsearch bulk reindex completed. took={}, items={}",
                    resp.took(),
                    resp.items().size());
        }

        return ops.size();
    }

    public List<PolicyDocument> searchByKeyword(String keyword, int from, int size) throws IOException {
        String q = (keyword == null) ? "" : keyword.trim();

        SearchResponse<PolicyDocument> response = esClient.search(
                s -> s.index(INDEX)
                        .from(Math.max(from, 0))
                        .size(Math.min(Math.max(size, 1), 100))
                        .query(query -> query.bool(b -> {
                            if (q.isEmpty()) {
                                return b.must(m -> m.matchAll(ma -> ma));
                            }
                            return b.must(m -> m.multiMatch(mm ->
                                    mm.query(q).operator(Operator.And).fields("plcyNm^3", "description", "keywords")));
                        })),
                PolicyDocument.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .toList();
    }

    public List<PolicyDocument> search(PolicySearchCondition condition, int from, int size) throws IOException {
        Query query = policyQueryBuilder.build(condition);

        SearchResponse<PolicyDocument> response = esClient.search(
                s -> s.index(INDEX)
                        .from(Math.max(from, 0))
                        .size(Math.min(Math.max(size, 1), 100))
                        .query(query),
                PolicyDocument.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .toList();
    }

    public SearchResult searchWithTotal(PolicySearchCondition condition, int from, int size) throws IOException {
        Query query = policyQueryBuilder.build(condition);

        SearchResponse<PolicyDocument> response = esClient.search(
                s -> s.index(INDEX)
                        .from(Math.max(from, 0))
                        .size(Math.min(Math.max(size, 1), 100))
                        .query(query),
                PolicyDocument.class);

        List<PolicyDocument> documents = response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .toList();

        return new SearchResult(documents, response.hits().total().value());
    }

    public static class SearchResult {
        private final List<PolicyDocument> documents;
        private final long total;

        public SearchResult(List<PolicyDocument> documents, long total) {
            this.documents = documents;
            this.total = total;
        }

        public List<PolicyDocument> getDocuments() {
            return documents;
        }

        public long getTotal() {
            return total;
        }
    }
}
