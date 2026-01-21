package com.back.domain.welfare.policy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.api.policy")
public record YouthPolicyProperties(String url, String key) {}
