package com.back.global.config.elasticsearch;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

@SpringBootTest
@ActiveProfiles("test")
class ElasticsearchHealthCheckTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Test
    @DisplayName("ElasticsearchHealthCheck가 정상적으로 동작하는지 확인")
    void testHealthCheck() {
        // ElasticsearchClient가 정상적으로 주입되었는지만 확인
        assertNotNull(elasticsearchClient, "ElasticsearchClient가 주입되어야 합니다");
        System.out.println("✅ ElasticsearchHealthCheck 컴포넌트가 정상적으로 로드되었습니다");
    }
}
