package com.back.global.springBatch.elasticSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.back.domain.welfare.policy.document.PolicyDocument;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.mapper.PolicyDocumentMapper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyEsItemWriter implements ItemWriter<Policy> {
    private final ElasticsearchClient esClient;
    private final PolicyDocumentMapper policyDocumentMapper;
    private static final String INDEX = "policy";

    @Override
    public void write(Chunk<? extends Policy> chunk) throws Exception {
        // ES에 bulk 저장 로직
        // 예: elasticsearchOperations.save(chunk.getItems());
        if (chunk.isEmpty()) return;

        List<BulkOperation> ops = new ArrayList<>();

        for (Policy policy : chunk) {
            PolicyDocument doc = policyDocumentMapper.toDocument(policy);
            if (doc == null || doc.getPolicyId() == null) {
                continue;
            }

            ops.add(BulkOperation.of(b -> b.index(
                    i -> i.index(INDEX).id(String.valueOf(doc.getPolicyId())).document(doc))));
        }

        try {
            var resp = esClient.bulk(b -> b.operations(ops)); // .refresh(Refresh.True)) 제거
            if (resp.errors()) {
                // 에러 상세는 item별로 존재하므로, 우선 전체 에러만 로그로 남김 (필요 시 확장)
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
        } catch (IOException e) {
            log.error("ES 통신 중 오류 발생...", e);
            throw new RuntimeException(e);
        }

        log.info("Elasticsearch에 {}개의 데이터 동기화 완료...", chunk.size());
    }
}
