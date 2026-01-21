package com.back.domain.welfare.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.welfare.policy.config.YouthPolicyProperties;
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyFetchService {

    private final YouthPolicyProperties properties;

    private final WebClient webClient = WebClient.builder().build();

    public String fetchPolicies(PolicyFetchRequestDto requestDto) {

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(properties.url())
                        .queryParam("apiKeyNm", properties.key())
                        .queryParam("pageType", requestDto.pageType())
                        .queryParam("pageSize", requestDto.pageSize())
                        .queryParam("rtnType", requestDto.rtnType())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
