package com.back.global.config.elasticsearch;

import java.io.IOException;

import org.springframework.stereotype.Component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ElasticsearchHealthCheck {

    private final ElasticsearchClient esClient;

    @PostConstruct
    void check() throws IOException {
        System.out.println("ES ping = " + esClient.ping());
    }
}
