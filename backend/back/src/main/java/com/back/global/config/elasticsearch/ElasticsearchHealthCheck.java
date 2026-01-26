package com.back.global.config.elasticsearch;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "custom.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchHealthCheck {

    private final ElasticsearchClient esClient;

    @PostConstruct
    void check() {
        try {
            boolean ping = esClient.ping().value();
            if (ping) {
                log.info("Elasticsearch ping = true");
            } else {
                log.warn("Elasticsearch ping = false");
            }
        } catch (IOException e) {
            // ES가 죽어있어도 앱 기동 자체는 막지 않도록 방어
            log.warn("Elasticsearch ping 실패(서버 미기동/연결불가 가능)", e);
        } catch (Exception e) {
            log.warn("Elasticsearch ping 중 예상치 못한 오류", e);
        }
    }
}
