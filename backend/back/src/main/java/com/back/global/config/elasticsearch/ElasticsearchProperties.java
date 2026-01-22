package com.back.global.config.elasticsearch;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "custom.elasticsearch")
public class ElasticsearchProperties {
    private String host = "localhost";
    private int port = 9200;
    private String scheme = "http";
    private int connectionTimeout = 5000;
    private int socketTimeout = 60000;
}
