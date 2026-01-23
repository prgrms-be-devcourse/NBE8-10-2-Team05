package com.back.domain.welfare.policy.service;

import java.io.IOException;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyElasticSearchService {
    private final RestClient restClient;

    public String searchByKeyword(String keyword) throws IOException {
        String query = """
        {
          "query": {
            "match": {
              "plcyNm": "%s"
            }
          }
        }
        """.formatted(keyword);

        Request request = new Request("POST", "/policy/_search");
        request.setJsonEntity(query);

        Response response = restClient.performRequest(request);

        return EntityUtils.toString(response.getEntity());
    }
}
