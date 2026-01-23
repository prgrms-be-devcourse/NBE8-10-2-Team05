package com.back.domain.member.geo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.api.geo")
public record GeoApiProperties(String key, String url) {}
