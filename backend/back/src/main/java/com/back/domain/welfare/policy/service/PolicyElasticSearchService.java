package com.back.domain.welfare.policy.service;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import com.back.domain.welfare.policy.document.PolicyDocument;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyElasticSearchService {
    private final RestClient restClient;

    private final ElasticsearchClient esClient;

    public List<PolicyDocument> searchByKeyword(String keyword) throws IOException {

        SearchResponse<PolicyDocument> response = esClient.search(
                s -> s.index("policy").query(q -> q.match(m -> m.field("plcyNm").query(keyword))),
                PolicyDocument.class);

        return response.hits().hits().stream().map(hit -> hit.source()).toList();
    }
}
