package com.back.global.config.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig {

    private final ElasticsearchProperties properties;
    private RestClient restClient;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        log.info("Elasticsearch 연결 설정: {}://{}:{}", properties.getScheme(), properties.getHost(), properties.getPort());

        restClient = RestClient.builder(
                        new HttpHost(properties.getHost(), properties.getPort(), properties.getScheme()))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(properties.getConnectionTimeout())
                        .setSocketTimeout(properties.getSocketTimeout()))
                .build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }

    @PreDestroy
    public void close() {
        if (restClient != null) {
            try {
                restClient.close();
                log.info("Elasticsearch RestClient 리소스 정리 완료");
            } catch (Exception e) {
                log.error("Elasticsearch RestClient 종료 중 오류 발생", e);
            }
        }
    }
}
